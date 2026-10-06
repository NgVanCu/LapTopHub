package com.laptophub.payment.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "vnpay")
public record VnPayProperties(
        @NotBlank String tmnCode,
        @NotBlank String hashSecret,
        @NotBlank String payUrl,
        @NotBlank String returnUrl) {
}
