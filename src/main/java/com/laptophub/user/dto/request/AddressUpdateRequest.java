package com.laptophub.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressUpdateRequest(

        @NotBlank(message = "Tên người nhận không được để trống")
        @Size(max = 255, message = "Tên người nhận tối đa 255 ký tự")
        String recipientName,

        @NotBlank(message = "Số điện thoại không được để trống")
        @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
        String phone,

        @NotBlank(message = "Tỉnh/thành phố không được để trống")
        @Size(max = 255, message = "Tỉnh/thành phố tối đa 255 ký tự")
        String province,

        @NotBlank(message = "Phường/xã không được để trống")
        @Size(max = 255, message = "Phường/xã tối đa 255 ký tự")
        String ward,

        @NotBlank(message = "Địa chỉ chi tiết không được để trống")
        @Size(max = 500, message = "Địa chỉ chi tiết tối đa 500 ký tự")
        String streetAddress) {
}
