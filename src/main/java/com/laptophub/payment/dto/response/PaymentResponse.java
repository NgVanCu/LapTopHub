package com.laptophub.payment.dto.response;

import com.laptophub.payment.entity.Payment;
import com.laptophub.payment.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        Long id,
        Long orderId,
        BigDecimal amount,
        PaymentStatus status,
        String gatewayTransactionNo,
        Instant expiresAt,
        Instant paidAt,
        Instant createdAt) {

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(payment.getId(), payment.getOrderId(), payment.getAmount(), payment.getStatus(),
                payment.getGatewayTransactionNo(), payment.getExpiresAt(), payment.getPaidAt(),
                payment.getCreatedAt());
    }
}
