package com.laptophub.voucher.service;

import com.laptophub.voucher.dto.response.VoucherValidationResult;
import com.laptophub.voucher.entity.Voucher;
import com.laptophub.voucher.entity.VoucherUsage;
import com.laptophub.voucher.enums.VoucherDiscountType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;

public interface VoucherService {
    VoucherValidationResult validate(String code, Long userId, BigDecimal orderAmount);

    VoucherUsage redeem(Long voucherId, Long orderId, Long userId, BigDecimal discountAmount);

    Voucher getByIdOrThrow(Long id);

    Page<Voucher> listAdmin(String code, Boolean active, Pageable pageable);

    Voucher create(String code, String description, VoucherDiscountType discountType, BigDecimal discountValue,
                   BigDecimal maxDiscountAmount, BigDecimal minOrderAmount, Integer usageLimit, Integer usageLimitPerUser,
                   Instant startAt, Instant endAt);

    Voucher update(Long id, String description, VoucherDiscountType discountType, BigDecimal discountValue,
                   BigDecimal maxDiscountAmount, BigDecimal minOrderAmount, Integer usageLimit, Integer usageLimitPerUser,
                   Instant startAt, Instant endAt, boolean active);
}
