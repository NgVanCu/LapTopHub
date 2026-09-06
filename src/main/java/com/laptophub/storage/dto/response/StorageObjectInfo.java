package com.laptophub.storage.dto.response;

public record StorageObjectInfo(
        String contentType,
        long sizeBytes,
        String etag
) {}