package com.laptophub.storage.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ConfirmUploadRequest(
        @NotBlank String objectKey
) {}
