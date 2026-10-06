package com.laptophub.payment.entity;

import com.laptophub.payment.enums.PaymentStatus;
import com.laptophub.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "gateway_txn_ref", nullable = false, length = 100, unique = true)
    private String gatewayTxnRef;

    @Column(name = "gateway_transaction_no", length = 100)
    private String gatewayTransactionNo;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "paid_at")
    private Instant paidAt;

    private Payment(Long orderId, BigDecimal amount, String gatewayTxnRef, Instant expiresAt) {
        this.orderId = Objects.requireNonNull(orderId, "orderId không được để trống");
        this.amount = Objects.requireNonNull(amount, "amount không được để trống");
        this.status = PaymentStatus.PENDING;
        this.gatewayTxnRef = Objects.requireNonNull(gatewayTxnRef, "gatewayTxnRef không được để trống");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt không được để trống");
    }

    public static Payment create(Long orderId, BigDecimal amount, String gatewayTxnRef, Instant expiresAt) {
        return new Payment(orderId, amount, gatewayTxnRef, expiresAt);
    }
}
