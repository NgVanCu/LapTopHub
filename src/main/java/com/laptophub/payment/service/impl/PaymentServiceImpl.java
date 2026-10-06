package com.laptophub.payment.service.impl;

import com.laptophub.payment.entity.Payment;
import com.laptophub.payment.enums.PaymentStatus;
import com.laptophub.payment.repository.PaymentRepository;
import com.laptophub.payment.service.PaymentService;
import com.laptophub.shared.exception.AppException;
import com.laptophub.shared.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {
    private static final Duration SESSION_TTL = Duration.ofMinutes(15);

    private final PaymentRepository paymentRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional
    public Payment createForOrder(Long orderId, BigDecimal amount) {
        Instant now = Instant.now();
        Payment payment = Payment.create(orderId, amount, generateTxnRef(orderId, now), now.plus(SESSION_TTL));
        return paymentRepository.save(payment);
    }

    // Dùng cho cả lần đầu lấy URL thanh toán lẫn mọi lần thử lại (kể cả sau
    // FAILED) — luôn sinh gatewayTxnRef/expiresAt mới (xem lý do ở
    // PaymentRepository.prepareForRetry).
    @Override
    @Transactional
    public Payment prepareForRetry(Long orderId) {
        Payment payment = getByOrderIdOrThrow(orderId);
        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_STATUS, "Đơn hàng đã được thanh toán");
        }
        Instant now = Instant.now();
        int rows = paymentRepository.prepareForRetry(payment.getId(), generateTxnRef(orderId, now),
                now.plus(SESSION_TTL));
        if (rows == 0) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_STATUS);
        }
        return getByOrderIdOrThrow(orderId);
    }

    // Best-effort, gọi từ OrderService khi hủy đơn — no-op nếu đơn COD (không
    // có payment) hoặc payment đã ở trạng thái cuối (PAID/CANCELLED).
    @Override
    @Transactional
    public void cancelIfPending(Long orderId) {
        paymentRepository.findByOrderId(orderId)
                .filter(payment -> payment.getStatus() == PaymentStatus.PENDING
                        || payment.getStatus() == PaymentStatus.FAILED)
                .ifPresent(payment -> paymentRepository.markCancelled(payment.getId()));
    }

    @Override
    public Optional<Payment> findByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    // Batch cho danh sách đơn (Customer/AdminOrderController qua
    // OrderService.toSummaryResponses) — tránh N+1 khi build cả trang. Đơn
    // không có key trong map nghĩa là COD (không có Payment).
    @Override
    public Map<Long, PaymentStatus> findStatusesByOrderIds(List<Long> orderIds) {
        return paymentRepository.findByOrderIdIn(orderIds).stream()
                .collect(Collectors.toMap(Payment::getOrderId, Payment::getStatus));
    }

    @Override
    public Payment getByOrderIdOrThrow(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Override
    public Optional<Payment> findByGatewayTxnRef(String gatewayTxnRef) {
        return paymentRepository.findByGatewayTxnRef(gatewayTxnRef);
    }

    @Override
    public Payment getByIdOrThrow(Long id) {
        return paymentRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    // Dùng bởi PublicVnPayController khi xử lý IPN — trả true nếu update có
    // hiệu lực (payment đang PENDING), false nếu đã xử lý trước đó/không còn
    // ở trạng thái hợp lệ (nguồn xác định idempotent, xem PaymentRepository).
    @Override
    @Transactional
    public boolean markPaid(Long paymentId, String gatewayTransactionNo, Instant paidAt) {
        return paymentRepository.markPaid(paymentId, gatewayTransactionNo, paidAt) > 0;
    }

    @Override
    @Transactional
    public boolean markFailed(Long paymentId) {
        return paymentRepository.markFailed(paymentId) > 0;
    }

    // Dùng bởi PaymentExpirySweepScheduler — chỉ liệt kê ứng viên, không phải
    // gate atomic (xem cancelIfExpired).
    @Override
    public List<Payment> listExpiredPending(Instant now) {
        return paymentRepository.findExpiredPending(now);
    }

    // Gate atomic thật sự cho payment-expiry sweep — trả false nếu payment đã
    // PAID/CANCELLED, hoặc vừa được gia hạn qua prepareForRetry (expiresAt
    // không còn < now). Chỉ khi trả true, caller mới được phép đụng tới
    // Order/inventory (xem OrderService.cancelExpiredOnlineOrder).
    @Override
    @Transactional
    public boolean cancelIfExpired(Long paymentId, Instant now) {
        return paymentRepository.markCancelledIfExpired(paymentId, now) > 0;
    }

    // Chỉ đọc — Admin không có endpoint sửa trạng thái Payment thủ công
    // (trạng thái chỉ đổi qua /ipn hoặc cancelIfPending), đúng nguyên tắc
    // "không dùng dữ liệu frontend làm nguồn xác nhận thanh toán".
    @Override
    public Page<Payment> listAdmin(PaymentStatus status, Long orderId, Pageable pageable) {
        return paymentRepository.search(status, orderId, pageable);
    }

    // orderId là duy nhất/đơn (UNIQUE order_id), timestamp phân biệt các lần
    // retry khác nhau trong ngày — đủ để tránh trùng vnp_TxnRef mà không cần
    // random/UUID.
    private String generateTxnRef(Long orderId, Instant now) {
        return orderId + "_" + now.toEpochMilli();
    }
}
