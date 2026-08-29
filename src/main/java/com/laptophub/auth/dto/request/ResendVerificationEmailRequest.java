package com.laptophub.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResendVerificationEmailRequest(

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ", regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")
        @Size(max = 255, message = "Email tối đa 255 ký tự")
        String email) {
}
