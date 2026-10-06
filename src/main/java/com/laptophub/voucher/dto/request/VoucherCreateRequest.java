package com.laptophub.voucher.dto.request;

import com.laptophub.voucher.enums.VoucherDiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public record VoucherCreateRequest(

        @NotBlank(message = "Mã voucher không được để trống")
        @Size(max = 50, message = "Mã voucher tối đa 50 ký tự")
        String code,

        @Size(max = 255, message = "Mô tả tối đa 255 ký tự")
        String description,

        @NotNull(message = "Loại giảm giá không được để trống")
        VoucherDiscountType discountType,

        @NotNull(message = "Giá trị giảm giá không được để trống")
        @Positive(message = "Giá trị giảm giá phải lớn hơn 0")
        BigDecimal discountValue,

        @PositiveOrZero(message = "Mức giảm tối đa không được âm")
        BigDecimal maxDiscountAmount,

        @NotNull(message = "Đơn tối thiểu không được để trống")
        @PositiveOrZero(message = "Đơn tối thiểu không được âm")
        BigDecimal minOrderAmount,

        @Positive(message = "Giới hạn lượt dùng phải lớn hơn 0")
        Integer usageLimit,

        @Positive(message = "Giới hạn lượt dùng mỗi người phải lớn hơn 0")
        Integer usageLimitPerUser,

        @NotNull(message = "Thời gian bắt đầu không được để trống")
        Instant startAt,

        @NotNull(message = "Thời gian kết thúc không được để trống")
        Instant endAt) {
}
