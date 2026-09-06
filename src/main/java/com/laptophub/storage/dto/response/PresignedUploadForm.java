package com.laptophub.storage.dto.response;

import java.util.Map;

public record PresignedUploadForm(
        String uploadUrl,
        String objectKey,
        Map<String, String> formFields,
        long expirySeconds
) {}
