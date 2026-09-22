package com.laptophub.storage.dto.response;

import java.util.List;

public record BatchPresignedUploadResponse(
        List<PresignedUploadForm> uploads
) {}
