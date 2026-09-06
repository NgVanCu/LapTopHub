package com.laptophub.storage.service.impl;

import com.laptophub.shared.exception.AppException;
import com.laptophub.shared.exception.ErrorCode;
import com.laptophub.shared.properties.MinioProperties;
import com.laptophub.storage.dto.response.PresignedUploadForm;
import com.laptophub.storage.dto.response.StorageObjectInfo;
import com.laptophub.storage.service.StorageService;
import io.minio.*;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import io.minio.errors.ErrorResponseException;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class StorageServiceImpl implements StorageService {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    public StorageServiceImpl(MinioClient minioClient, MinioProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
    }

    @Override
    public PresignedUploadForm generateUploadForm(
            String objectKey,
            String contentType,
            long maxSizeBytes
    ) {

        if (!properties.allowedContentTypes().contains(contentType)) {
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
        }

        long configuredMaxSize =
                properties.maxFileSize().toBytes();

        if (maxSizeBytes <= 0 ||
                maxSizeBytes > configuredMaxSize) {

            throw new AppException(ErrorCode.FILE_TOO_LARGE);
        }

        try {

            long expirySeconds =
                    properties.presigned()
                            .uploadExpiration()
                            .toSeconds();

            PostPolicy policy = new PostPolicy(
                    properties.bucket(),
                    ZonedDateTime.now()
                            .plusSeconds(expirySeconds)
            );

            policy.addEqualsCondition(
                    "key",
                    objectKey
            );

            policy.addEqualsCondition(
                    "Content-Type",
                    contentType
            );

            policy.addContentLengthRangeCondition(
                    1,
                    maxSizeBytes
            );

            Map<String, String> formData =
                    minioClient.getPresignedPostFormData(policy);

            formData.put("key", objectKey);
            formData.put("Content-Type", contentType);

            String uploadUrl =
                    properties.endpoint().replaceAll("/$", "")
                            + "/"
                            + properties.bucket();

            return new PresignedUploadForm(
                    uploadUrl,
                    objectKey,
                    formData,
                    expirySeconds
            );

        } catch (Exception e) {

            log.error(
                    "Không thể tạo upload form cho object: {}",
                    objectKey,
                    e
            );

            throw new AppException(
                    ErrorCode.STORAGE_OPERATION_FAILED
            );
        }
    }

    @Override
    public String generateDownloadUrl(String objectKey) {
        verifyExists(objectKey);

        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Http.Method.GET)
                            .bucket(properties.bucket())
                            .object(objectKey)
                            .expiry((int) properties.presigned().downloadExpiration().toSeconds())
                            .build());
        } catch (Exception e) {
            log.error("Không thể tạo download URL cho object: {}", objectKey, e);
            throw new AppException(ErrorCode.STORAGE_OPERATION_FAILED);
        }
    }

    @Override
    public StorageObjectInfo verifyExists(String objectKey) {
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(properties.bucket())
                            .object(objectKey)
                            .build());

            return new StorageObjectInfo(stat.contentType(), stat.size(), stat.etag());
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                throw new AppException(ErrorCode.STORAGE_OBJECT_NOT_FOUND);
            }
            log.error("Lỗi khi kiểm tra object: {}", objectKey, e);
            throw new AppException(ErrorCode.STORAGE_OPERATION_FAILED);
        } catch (Exception e) {
            log.error("Lỗi khi kiểm tra object: {}", objectKey, e);
            throw new AppException(ErrorCode.STORAGE_OPERATION_FAILED);
        }
    }

    @Override
    public void copy(String sourceKey, String destinationKey) {
        verifyExists(sourceKey);

        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(properties.bucket())
                            .object(destinationKey)
                            .source(SourceObject.builder()
                                    .bucket(properties.bucket())
                                    .object(sourceKey)
                                    .build())
                            .build());
        } catch (Exception e) {
            log.error("Không thể copy object từ {} sang {}", sourceKey, destinationKey, e);
            throw new AppException(ErrorCode.STORAGE_OPERATION_FAILED);
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(properties.bucket())
                            .object(objectKey)
                            .build());
        } catch (Exception e) {
            log.error("Không thể xóa object: {}", objectKey, e);
            throw new AppException(ErrorCode.STORAGE_OPERATION_FAILED);
        }
    }

    @Override
    public List<String> listObjectsOlderThan(String prefix, Duration age) {
        List<String> expiredKeys = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(properties.bucket())
                            .prefix(prefix)
                            .recursive(true)
                            .build());

            ZonedDateTime cutoff = ZonedDateTime.now().minus(age);
            for (Result<Item> result : results) {
                Item item = result.get();
                if (item.lastModified().isBefore(cutoff)) {
                    expiredKeys.add(item.objectName());
                }
            }
        } catch (Exception e) {
            log.error("Không thể liệt kê object trong prefix: {}", prefix, e);
            throw new AppException(ErrorCode.STORAGE_OPERATION_FAILED);
        }
        return expiredKeys;
    }
}
