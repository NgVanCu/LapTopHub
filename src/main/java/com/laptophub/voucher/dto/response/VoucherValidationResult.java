package com.laptophub.voucher.dto.response;

import com.laptophub.voucher.entity.Voucher;

import java.math.BigDecimal;

public record VoucherValidationResult(Voucher voucher, BigDecimal discountAmount) {
}
