package com.laptophub.voucher.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VoucherValidateRequest(

        @NotBlank(message = "Mã voucher không được để trống")
        @Size(max = 50, message = "Mã voucher tối đa 50 ký tự")
        String code) {
}
