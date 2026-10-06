package com.laptophub.voucher.entity;

import com.laptophub.shared.entity.BaseEntity;
import com.laptophub.voucher.enums.VoucherDiscountType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "vouchers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Voucher extends BaseEntity {

    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;

    @Column(name = "description", length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private VoucherDiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "max_discount_amount", precision = 12, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "min_order_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal minOrderAmount;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "usage_limit_per_user")
    private Integer usageLimitPerUser;

    @Column(name = "used_count", nullable = false)
    private Integer usedCount;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    @Column(name = "active", nullable = false)
    private boolean active;

    private Voucher(String code, String description, VoucherDiscountType discountType, BigDecimal discountValue,
                    BigDecimal maxDiscountAmount, BigDecimal minOrderAmount, Integer usageLimit, Integer usageLimitPerUser,
                    Instant startAt, Instant endAt) {
        this.code = Objects.requireNonNull(code, "code không được để trống");
        this.description = description;
        this.discountType = Objects.requireNonNull(discountType, "discountType không được để trống");
        this.discountValue = Objects.requireNonNull(discountValue, "discountValue không được để trống");
        this.maxDiscountAmount = maxDiscountAmount;
        this.minOrderAmount = Objects.requireNonNull(minOrderAmount, "minOrderAmount không được để trống");
        this.usageLimit = usageLimit;
        this.usageLimitPerUser = usageLimitPerUser;
        this.usedCount = 0;
        this.startAt = Objects.requireNonNull(startAt, "startAt không được để trống");
        this.endAt = Objects.requireNonNull(endAt, "endAt không được để trống");
        this.active = true;
    }

    public static Voucher create(String code, String description, VoucherDiscountType discountType,
                                 BigDecimal discountValue, BigDecimal maxDiscountAmount, BigDecimal minOrderAmount, Integer usageLimit,
                                 Integer usageLimitPerUser, Instant startAt, Instant endAt) {
        return new Voucher(code, description, discountType, discountValue, maxDiscountAmount, minOrderAmount,
                usageLimit, usageLimitPerUser, startAt, endAt);
    }

    public void update(String description, VoucherDiscountType discountType, BigDecimal discountValue,
                       BigDecimal maxDiscountAmount, BigDecimal minOrderAmount, Integer usageLimit, Integer usageLimitPerUser,
                       Instant startAt, Instant endAt, boolean active) {
        this.description = description;
        this.discountType = Objects.requireNonNull(discountType, "discountType không được để trống");
        this.discountValue = Objects.requireNonNull(discountValue, "discountValue không được để trống");
        this.maxDiscountAmount = maxDiscountAmount;
        this.minOrderAmount = Objects.requireNonNull(minOrderAmount, "minOrderAmount không được để trống");
        this.usageLimit = usageLimit;
        this.usageLimitPerUser = usageLimitPerUser;
        this.startAt = Objects.requireNonNull(startAt, "startAt không được để trống");
        this.endAt = Objects.requireNonNull(endAt, "endAt không được để trống");
        this.active = active;
    }

    public boolean isWithinWindow(Instant now) {
        return !now.isBefore(startAt) && !now.isAfter(endAt);
    }

    public boolean meetsMinOrder(BigDecimal orderAmount) {
        return orderAmount.compareTo(minOrderAmount) >= 0;
    }

    public BigDecimal computeDiscount(BigDecimal orderAmount) {
        BigDecimal discount;
        if (discountType == VoucherDiscountType.PERCENTAGE) {
            discount = orderAmount.multiply(discountValue)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (maxDiscountAmount != null && discount.compareTo(maxDiscountAmount) > 0) {
                discount = maxDiscountAmount;
            }
        } else {
            discount = discountValue;
        }
        if (discount.compareTo(orderAmount) > 0) {
            discount = orderAmount;
        }
        return discount;
    }
}
