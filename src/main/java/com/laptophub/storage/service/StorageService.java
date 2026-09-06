package com.laptophub.storage.service;



import com.laptophub.storage.dto.response.PresignedUploadForm;
import com.laptophub.storage.dto.response.StorageObjectInfo;

import java.time.Duration;
import java.util.List;

public interface StorageService {

    PresignedUploadForm generateUploadForm(String objectKey, String contentType, long maxSizeBytes);

    String generateDownloadUrl(String objectKey);

    StorageObjectInfo verifyExists(String objectKey);

    void copy(String sourceKey, String destinationKey);

    void delete(String objectKey);


    List<String> listObjectsOlderThan(String prefix, Duration age);
}
