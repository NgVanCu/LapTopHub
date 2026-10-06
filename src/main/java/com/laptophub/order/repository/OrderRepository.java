package com.laptophub.order.repository;

import com.laptophub.order.dto.projection.OrderStatusCountProjection;
import com.laptophub.order.dto.projection.RevenueByDayProjection;
import com.laptophub.order.entity.Order;
import com.laptophub.order.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserId(Long userId, Pageable pageable);

    // Dùng để kiểm tra ownership khi Customer xem chi tiết 1 đơn cụ thể —
    // không tồn tại hoặc không phải của user này đều coi như not found, giống
    // AddressRepository.findByIdAndUserId.
    Optional<Order> findByIdAndUserId(Long id, Long userId);

    // Dùng cho Admin liệt kê đơn của mọi Customer, lọc theo trạng thái (tùy
    // chọn) — mirror StockReceiptRepository.search.
    @Query("SELECT o FROM Order o WHERE (:status IS NULL OR o.status = :status) ORDER BY o.createdAt DESC")
    Page<Order> search(@Param("status") OrderStatus status, Pageable pageable);

    // Native query — JPQL không có date-truncation chuẩn cho GROUP BY theo
    // ngày. Chỉ tính đơn DELIVERED (doanh thu "thực nhận", đã chốt cùng
    // user ở Giai đoạn 9) — không JOIN payments vì đơn ONLINE muốn qua được
    // CONFIRMED trở lên đã bắt buộc Payment.status = PAID (xem
    // OrderService.confirm). from/to null nghĩa là không giới hạn 1 phía;
    // to là cận trên loại trừ (exclusive) — DashboardService truyền vào
    // start-of-day của ngày kế tiếp `to`.
    @Query(value = """
            select date(created_at) as day, sum(total_amount) as revenue, count(*) as order_count
            from orders
            where status = 'DELIVERED'
              and (:from is null or created_at >= :from)
              and (:to is null or created_at < :to)
            group by date(created_at)
            order by date(created_at)
            """, nativeQuery = true)
    List<RevenueByDayProjection> findRevenueByDay(@Param("from") Instant from, @Param("to") Instant to);

    // Không lọc theo status (khác revenue) — "đơn theo trạng thái" muốn thấy
    // toàn bộ vòng đời, kể cả PENDING/CANCELLED. Luôn trả về đủ các status có
    // ít nhất 1 đơn, không phân trang (tối đa 8 dòng, đúng số lượng
    // OrderStatus).
    @Query("""
            select o.status as status, count(o) as count
            from Order o
            where (:from is null or o.createdAt >= :from) and (:to is null or o.createdAt < :to)
            group by o.status
            """)
    List<OrderStatusCountProjection> countByStatusGrouped(@Param("from") Instant from, @Param("to") Instant to);

    // 3 gate atomic dưới đây chống double-processing khi 2 request cùng thao
    // tác trên 1 đơn gần như đồng thời (double-ship, double-cancel,
    // double-approve-return) — mỗi UPDATE ... WHERE status = ... là 1
    // statement atomic ở tầng InnoDB, luôn đọc dữ liệu committed mới nhất +
    // khoá row (khác SELECT thường dùng snapshot REPEATABLE READ), nên chỉ 1
    // trong N request đồng thời có thể thắng. Service phải gọi các method này
    // TRƯỚC khi đụng InventoryService, không phải sau.

    // Chỉ transaction thắng mới được gọi InventoryService.fulfill (xem
    // OrderService.ship) — chống double-fulfill khi tồn kho variant đủ lớn để
    // "trông như" hợp lệ ở cả 2 lần gọi.
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Order o SET o.status = 'SHIPPING' WHERE o.id = :id AND o.status = 'PREPARING'")
    int shipIfPreparing(@Param("id") Long id);

    // Danh sách trạng thái khớp chính xác Order.CANCELLABLE_STATUSES/
    // isCancellable(). Chỉ transaction thắng mới được gọi
    // InventoryService.release (xem OrderService.cancelInternal) — KHÔNG dựa
    // vào điều kiện reservedQuantity >= quantity của InventoryBalanceRepository
    // để chống double-release, vì reservedQuantity là tổng hợp của mọi đơn
    // đang giữ hàng cho variant đó, không riêng đơn này.
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Order o SET o.status = 'CANCELLED' WHERE o.id = :id AND o.status IN ('PENDING','CONFIRMED','PREPARING')")
    int cancelIfCancellable(@Param("id") Long id);

    // Chỉ transaction thắng mới được gọi InventoryService.receiveReturn (xem
    // OrderService.approveReturn) — chống double hoàn tồn khi Admin
    // double-click "approve" hoặc 2 request approve gần như đồng thời.
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Order o SET o.status = 'RETURNED' WHERE o.id = :id AND o.status = 'RETURN_REQUESTED'")
    int approveReturnIfRequested(@Param("id") Long id);
}

