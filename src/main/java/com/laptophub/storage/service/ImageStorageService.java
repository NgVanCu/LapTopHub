package com.laptophub.storage.service;

import com.laptophub.storage.dto.request.ConfirmUploadRequest;
import com.laptophub.storage.dto.request.PresignRequest;
import com.laptophub.storage.dto.response.PresignedUploadForm;
import com.laptophub.storage.enums.ImagePurpose;

import java.util.List;

public interface ImageStorageService {

    PresignedUploadForm requestUpload(ImagePurpose purpose, PresignRequest request);

    List<PresignedUploadForm> requestUploads(
            ImagePurpose purpose,
            List<PresignRequest> requests
    );

    String confirmUpload(ImagePurpose purpose, Long ownerId, ConfirmUploadRequest request);

    String generateDownloadUrl(String objectKey);

    void delete(String objectKey);
}
