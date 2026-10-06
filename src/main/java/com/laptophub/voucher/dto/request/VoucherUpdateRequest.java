package com.laptophub.voucher.dto.request;

import com.laptophub.voucher.enums.VoucherDiscountType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

// Không có field code — không cho đổi code sau khi tạo (xem Voucher.update).
public record VoucherUpdateRequest(

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
        Instant endAt,

        @NotNull(message = "active không được để trống")
        Boolean active) {
}
