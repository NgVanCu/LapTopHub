package com.laptophub.storage.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PresignRequest(
        @NotBlank String filename,
        @NotBlank String contentType,
        @NotNull @Positive long size
) {}