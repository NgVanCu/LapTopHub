package com.laptophub.payment.repository;

import com.laptophub.payment.entity.Payment;
import com.laptophub.payment.enums.PaymentStatus;
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
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    // Batch cho danh sách đơn (Customer/AdminOrderController) — tránh N+1 khi
    // cần biết đơn nào đã thanh toán xong trong 1 trang phân trang.
    List<Payment> findByOrderIdIn(List<Long> orderIds);

    Optional<Payment> findByGatewayTxnRef(String gatewayTxnRef);

    @Query("SELECT p FROM Payment p WHERE (:status IS NULL OR p.status = :status) "
            + "AND (:orderId IS NULL OR p.orderId = :orderId) ORDER BY p.createdAt DESC")
    Page<Payment> search(@Param("status") PaymentStatus status, @Param("orderId") Long orderId, Pageable pageable);

    // Dùng cho lần đầu lấy URL thanh toán lẫn mọi lần thử lại (kể cả sau khi
    // đã FAILED) — sinh gatewayTxnRef/expiresAt mới, không tái dùng ref cũ
    // (VNPay không cho trùng vnp_TxnRef trong cùng ngày).
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Payment p SET p.status = 'PENDING', p.gatewayTxnRef = :gatewayTxnRef, "
            + "p.expiresAt = :expiresAt WHERE p.id = :id AND p.status IN ('PENDING', 'FAILED')")
    int prepareForRetry(@Param("id") Long id, @Param("gatewayTxnRef") String gatewayTxnRef,
                        @Param("expiresAt") Instant expiresAt);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Payment p SET p.status = 'PAID', p.gatewayTransactionNo = :gatewayTransactionNo, "
            + "p.paidAt = :paidAt WHERE p.id = :id AND p.status = 'PENDING'")
    int markPaid(@Param("id") Long id, @Param("gatewayTransactionNo") String gatewayTransactionNo,
                 @Param("paidAt") Instant paidAt);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Payment p SET p.status = 'FAILED' WHERE p.id = :id AND p.status = 'PENDING'")
    int markFailed(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Payment p SET p.status = 'CANCELLED' WHERE p.id = :id AND p.status IN ('PENDING', 'FAILED')")
    int markCancelled(@Param("id") Long id);

    // Candidate query cho PaymentExpirySweepScheduler — CHỈ để liệt kê ứng
    // viên, không phải gate atomic (không cần chống race ở đây, kết quả có
    // thể "cũ" ngay khi trả về). Gate atomic thật sự là markCancelledIfExpired
    // bên dưới, chạy riêng cho từng payment tại đúng thời điểm xử lý.
    @Query("SELECT p FROM Payment p WHERE p.status IN ('PENDING', 'FAILED') AND p.expiresAt < :now")
    List<Payment> findExpiredPending(@Param("now") Instant now);

    // Gate atomic cho payment-expiry sweep — chỉ hủy nếu NGAY TẠI THỜI ĐIỂM
    // update payment vẫn (a) chưa PAID và (b) vẫn thực sự hết hạn. Xác nhận
    // LẠI cả 2 điều kiện thay vì tin vào kết quả findExpiredPending đã cũ:
    // nếu IPN vừa markPaid thành công, điều kiện status IN (...) fail, 0
    // dòng; nếu customer vừa prepareForRetry (expiresAt dời sang tương lai),
    // điều kiện expiresAt < :now fail, 0 dòng — cả 2 trường hợp sweep đều
    // đúng bỏ qua, không đụng gì tới Order/inventory (xem
    // OrderService.cancelExpiredOnlineOrder).
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Payment p SET p.status = 'CANCELLED' WHERE p.id = :id "
            + "AND p.status IN ('PENDING', 'FAILED') AND p.expiresAt < :now")
    int markCancelledIfExpired(@Param("id") Long id, @Param("now") Instant now);
}
