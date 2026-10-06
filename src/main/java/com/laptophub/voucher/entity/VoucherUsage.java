package com.laptophub.voucher.entity;

import com.laptophub.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "voucher_usages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VoucherUsage extends BaseEntity {

    @Column(name = "voucher_id", nullable = false)
    private Long voucherId;

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;

    private VoucherUsage(Long voucherId, Long orderId, Long userId, BigDecimal discountAmount) {
        this.voucherId = Objects.requireNonNull(voucherId, "voucherId không được để trống");
        this.orderId = Objects.requireNonNull(orderId, "orderId không được để trống");
        this.userId = Objects.requireNonNull(userId, "userId không được để trống");
        this.discountAmount = Objects.requireNonNull(discountAmount, "discountAmount không được để trống");
    }

    public static VoucherUsage create(Long voucherId, Long orderId, Long userId, BigDecimal discountAmount) {
        return new VoucherUsage(voucherId, orderId, userId, discountAmount);
    }
}
