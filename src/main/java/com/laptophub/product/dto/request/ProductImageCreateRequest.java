package com.laptophub.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductImageCreateRequest(

        @NotBlank(message = "URL ảnh không được để trống")
        @Size(max = 500, message = "URL ảnh tối đa 500 ký tự")
        String objectKey,

        @Size(max = 255, message = "Alt text tối đa 255 ký tự")
        String altText) {
}

