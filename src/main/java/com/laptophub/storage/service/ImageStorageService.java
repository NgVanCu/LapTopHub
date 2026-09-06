package com.laptophub.storage.service;

import com.laptophub.storage.dto.request.ConfirmUploadRequest;
import com.laptophub.storage.dto.request.PresignRequest;
import com.laptophub.storage.dto.response.PresignedUploadForm;
import com.laptophub.storage.enums.ImagePurpose;

public interface ImageStorageService {

    PresignedUploadForm requestUpload(ImagePurpose purpose, PresignRequest request);

    String confirmUpload(ImagePurpose purpose, Long ownerId, ConfirmUploadRequest request);

    String generateDownloadUrl(String objectKey);

    void delete(String objectKey);
}
