package com.laptophub.shared.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.Set;

@Validated
@ConfigurationProperties(prefix = "storage.minio")
public record MinioProperties(
        @NotBlank String endpoint,
        @NotBlank String accessKey,
        @NotBlank String secretKey,
        @NotBlank String bucket,
        @NotNull PresignedProperties presigned,
        @NotNull DataSize maxFileSize,
        @NotEmpty Set<String> allowedContentTypes,
        @NotNull CleanupProperties cleanup
) {
    public record PresignedProperties(
            @NotNull Duration uploadExpiration,
            @NotNull Duration downloadExpiration
    ) {}
    public record CleanupProperties(
            boolean enabled,
            Duration interval,
            Duration orphanAge
    ) {}
}
