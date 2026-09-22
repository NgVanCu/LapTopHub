package com.laptophub.storage.controller;

import com.laptophub.shared.response.ApiResponse;
import com.laptophub.storage.dto.request.BatchPresignRequest;
import com.laptophub.storage.dto.request.ConfirmUploadRequest;
import com.laptophub.storage.dto.request.PresignRequest;
import com.laptophub.storage.dto.response.BatchPresignedUploadResponse;
import com.laptophub.storage.dto.response.PresignedUploadForm;
import com.laptophub.storage.enums.ImagePurpose;
import com.laptophub.storage.service.ImageStorageService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/storage/images")
public class StorageController {
    private final ImageStorageService imageStorageService;
    public StorageController(ImageStorageService imageStorageService) {
        this.imageStorageService = imageStorageService;
    }

    @PostMapping("/presign")
    public ApiResponse<PresignedUploadForm> requestUpload(
            @RequestParam ImagePurpose purpose,
            @Valid @RequestBody PresignRequest request
    ) {

        PresignedUploadForm response =
                imageStorageService.requestUpload(
                        purpose,
                        request
                );

        return ApiResponse.success("ok",response);
    }

    @PostMapping("/presign/batch")
    public ApiResponse<BatchPresignedUploadResponse> requestUploads(
            @RequestParam ImagePurpose purpose,
            @Valid @RequestBody BatchPresignRequest request
    ) {
        List<PresignedUploadForm> uploads =
                imageStorageService.requestUploads(
                        purpose,
                        request.files()
                );

        BatchPresignedUploadResponse response =
                new BatchPresignedUploadResponse(uploads);

        return ApiResponse.success("ok", response);
    }

    @PostMapping("/confirm")
    public ApiResponse<String> confirmUpload(
            @RequestParam ImagePurpose purpose,
            @RequestParam Long ownerId,
            @Valid @RequestBody ConfirmUploadRequest request
    ) {

        String objectKey =
                imageStorageService.confirmUpload(
                        purpose,
                        ownerId,
                        request
                );

        return ApiResponse.success("ok",objectKey);
    }

    @GetMapping("/url")
    public ApiResponse<String> generateDownloadUrl(
            @RequestParam String objectKey
    ) {

        String url =
                imageStorageService.generateDownloadUrl(
                        objectKey
                );

        return ApiResponse.success("ok",url);
    }

}

