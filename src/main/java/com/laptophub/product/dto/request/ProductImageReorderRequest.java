package com.laptophub.product.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ProductImageReorderRequest(

        @NotEmpty(message = "Danh sách ảnh không được để trống")
        List<@NotNull(message = "Image ID không được để trống") Long> orderedImageIds) {
}
