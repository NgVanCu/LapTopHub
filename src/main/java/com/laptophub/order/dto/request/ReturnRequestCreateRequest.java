package com.laptophub.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReturnRequestCreateRequest(

        @NotBlank(message = "Lý do trả hàng không được để trống")
        @Size(max = 500, message = "Lý do trả hàng tối đa 500 ký tự")
        String reason) {
}
