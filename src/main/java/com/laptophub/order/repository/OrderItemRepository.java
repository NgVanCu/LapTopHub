package com.laptophub.order.repository;

import com.laptophub.order.dto.projection.BestSellingProjection;
import com.laptophub.order.entity.OrderItem;
import com.laptophub.order.enums.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    // orderId là FK phẳng (không @ManyToOne) nên không dựng được derived query
    // kiểu findByOrder_UserId — dùng subquery trên Order (cùng module), mirror
    // ProductRepository.searchPublic's "exists (select ...)" pattern. Dùng bởi
    // OrderService.findDeliveredOrderIdForProduct — review module kiểm tra
    // quyền đánh giá qua service, không đọc thẳng repository này.
    @Query("""
            select oi from OrderItem oi
            where oi.orderId in (
                select o.id from Order o where o.userId = :userId and o.status = :status
            )
            """)
    List<OrderItem> findByOrderUserIdAndOrderStatus(@Param("userId") Long userId, @Param("status") OrderStatus status);

    // Chỉ tính đơn DELIVERED (đồng nhất với OrderRepository.findRevenueByDay
    // — cùng 1 định nghĩa "đơn hợp lệ để tính" xuyên suốt dashboard). Trả
    // List<T> (không Page<T>) vì Spring Data JPA không tự sinh đúng COUNT
    // query khi phân trang 1 query có GROUP BY — Pageable ở đây chỉ dùng để
    // giới hạn số dòng trả về (top N theo số lượng bán), không phải phân
    // trang thật.
    @Query("""
            select oi.productVariantId as productVariantId, sum(oi.quantity) as quantitySold,
                   sum(oi.unitPrice * oi.quantity) as revenue
            from OrderItem oi
            where oi.orderId in (
                select o.id from Order o
                where o.status = 'DELIVERED'
                  and (:from is null or o.createdAt >= :from)
                  and (:to is null or o.createdAt < :to)
            )
            group by oi.productVariantId
            order by sum(oi.quantity) desc
            """)
    List<BestSellingProjection> findBestSelling(@Param("from") Instant from, @Param("to") Instant to,
                                                Pageable pageable);
}
