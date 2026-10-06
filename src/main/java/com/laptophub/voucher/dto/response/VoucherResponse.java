package com.laptophub.voucher.dto.response;

import com.laptophub.voucher.entity.Voucher;
import com.laptophub.voucher.enums.VoucherDiscountType;

import java.math.BigDecimal;
import java.time.Instant;

public record VoucherResponse(
        Long id,
        String code,
        String description,
        VoucherDiscountType discountType,
        BigDecimal discountValue,
        BigDecimal maxDiscountAmount,
        BigDecimal minOrderAmount,
        Integer usageLimit,
        Integer usageLimitPerUser,
        Integer usedCount,
        Instant startAt,
        Instant endAt,
        boolean active,
        Instant createdAt) {

    public static VoucherResponse from(Voucher voucher) {
        return new VoucherResponse(voucher.getId(), voucher.getCode(), voucher.getDescription(),
                voucher.getDiscountType(), voucher.getDiscountValue(), voucher.getMaxDiscountAmount(),
                voucher.getMinOrderAmount(), voucher.getUsageLimit(), voucher.getUsageLimitPerUser(),
                voucher.getUsedCount(), voucher.getStartAt(), voucher.getEndAt(), voucher.isActive(),
                voucher.getCreatedAt());
    }
}
