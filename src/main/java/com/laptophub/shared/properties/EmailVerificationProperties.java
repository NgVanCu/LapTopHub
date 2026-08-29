package com.laptophub.shared.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.email-verification")
public record EmailVerificationProperties(@NotNull Duration ttl, @NotBlank String verifyUrl) {
}