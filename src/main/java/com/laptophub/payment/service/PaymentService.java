package com.laptophub.payment.service;

import com.laptophub.payment.entity.Payment;
import com.laptophub.payment.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PaymentService {
    Payment createForOrder(Long orderId, BigDecimal amount);

    Payment prepareForRetry(Long orderId);

    void cancelIfPending(Long orderId);

    Optional<Payment> findByOrderId(Long orderId);

    Map<Long, PaymentStatus> findStatusesByOrderIds(List<Long> orderIds);

    Payment getByOrderIdOrThrow(Long orderId);

    Optional<Payment> findByGatewayTxnRef(String gatewayTxnRef);

    Payment getByIdOrThrow(Long id);

    boolean markPaid(Long paymentId, String gatewayTransactionNo, Instant paidAt);

    boolean markFailed(Long paymentId);

    List<Payment> listExpiredPending(Instant now);

    boolean cancelIfExpired(Long paymentId, Instant now);

    Page<Payment> listAdmin(PaymentStatus status, Long orderId, Pageable pageable);
}
