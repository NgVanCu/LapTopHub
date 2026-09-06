package com.laptophub.storage.service.impl;

import com.laptophub.shared.exception.AppException;
import com.laptophub.shared.exception.ErrorCode;
import com.laptophub.shared.properties.MinioProperties;
import com.laptophub.storage.dto.request.ConfirmUploadRequest;
import com.laptophub.storage.dto.request.PresignRequest;
import com.laptophub.storage.dto.response.PresignedUploadForm;
import com.laptophub.storage.dto.response.StorageObjectInfo;
import com.laptophub.storage.enums.ImagePurpose;
import com.laptophub.storage.service.ImageStorageService;
import com.laptophub.storage.service.StorageService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class ImageStorageServiceImpl implements ImageStorageService {
    private static final Map<String, String> CONTENT_TYPE_TO_EXT =
            Map.of(
                    "image/jpeg", "jpg",
                    "image/png", "png",
                    "image/webp", "webp"
            );

    private final StorageService storageService;
    private final MinioProperties properties;

    public ImageStorageServiceImpl(StorageService storageService, MinioProperties properties) {
        this.storageService = storageService;
        this.properties = properties;
    }

    @Override
    public PresignedUploadForm requestUpload(ImagePurpose purpose, PresignRequest request){
        String extension = CONTENT_TYPE_TO_EXT.get(request.contentType());
        if (extension == null) {
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
        }
        long maxBytes =
                properties.maxFileSize().toBytes();

        if (request.size() > maxBytes) {
            throw new AppException(
                    ErrorCode.FILE_TOO_LARGE
            );
        }
        String objectKey = generateTemporaryKey(purpose, extension);

        return storageService.generateUploadForm(
                objectKey,
                request.contentType(),
                maxBytes
        );
    }

    @Override
    public String confirmUpload(ImagePurpose purpose, Long ownerId, ConfirmUploadRequest request){
        String temporaryKey = request.objectKey();

        validateTemporaryKey(purpose, temporaryKey);
        StorageObjectInfo info = storageService.verifyExists(temporaryKey);
        validateObject(info);

        String extension = CONTENT_TYPE_TO_EXT.get(info.contentType());
        String finalKey = purpose.path() + "/"
                        + ownerId
                        + "/"
                        + UUID.randomUUID()
                        + "."
                        + extension;
        storageService.copy(temporaryKey, finalKey);

        storageService.delete(temporaryKey);
        return finalKey;
    }

    @Override
    public String generateDownloadUrl(String objectKey){
        return storageService.generateDownloadUrl(
                objectKey
        );
    }

    @Override
    public void delete(String objectKey) {

        if (objectKey == null || objectKey.isBlank()) {
            return;
        }
        storageService.delete(objectKey);
    }


    private String generateTemporaryKey(ImagePurpose purpose, String extension) {
        return purpose.path() + "/tmp/"
                + UUID.randomUUID()
                + "."
                + extension;
    }

    private void validateTemporaryKey(ImagePurpose purpose, String objectKey) {

        String expectedPrefix = purpose.path();

        if (!objectKey.startsWith(expectedPrefix)) {

            throw new AppException(
                    ErrorCode.INVALID_OBJECT_KEY
            );
        }
    }

    private void validateObject(StorageObjectInfo info) {

        if (!properties.allowedContentTypes().contains(info.contentType())) {
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
        }

        if (info.sizeBytes() > properties.maxFileSize().toBytes()) {
            throw new AppException(ErrorCode.FILE_TOO_LARGE);
        }
    }
}
