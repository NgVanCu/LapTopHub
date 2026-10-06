package com.laptophub.voucher.dto.response;

import java.math.BigDecimal;

public record VoucherValidateResponse(
        String code,
        BigDecimal orderAmount,
        BigDecimal discountAmount,
        BigDecimal finalAmount) {

    public static VoucherValidateResponse of(String code, BigDecimal orderAmount, BigDecimal discountAmount) {
        return new VoucherValidateResponse(code, orderAmount, discountAmount, orderAmount.subtract(discountAmount));
    }
}
