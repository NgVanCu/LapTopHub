package com.laptophub.review.dto.request;

import jakarta.validation.constraints.*;

public record ReviewCreateRequest(

        @NotNull(message = "Số sao đánh giá không được để trống")
        @Min(value = 1, message = "Số sao đánh giá tối thiểu là 1")
        @Max(value = 5, message = "Số sao đánh giá tối đa là 5")
        Integer rating,

        @NotBlank(message = "Nội dung đánh giá không được để trống")
        @Size(max = 2000, message = "Nội dung đánh giá tối đa 2000 ký tự")
        String comment) {
}
