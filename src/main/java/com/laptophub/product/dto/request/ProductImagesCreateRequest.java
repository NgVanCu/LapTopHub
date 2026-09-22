package com.laptophub.product.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ProductImagesCreateRequest(

        @NotEmpty(message = "Danh sách ảnh không được để trống")
        @Size(
                max = 10,
                message = "Tối đa được thêm 10 ảnh mỗi lần"
        )
        List<@Valid ProductImageCreateRequest> images

) {
}