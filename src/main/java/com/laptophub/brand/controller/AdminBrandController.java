package com.laptophub.brand.controller;

import com.laptophub.brand.dto.request.BrandCreateRequest;
import com.laptophub.brand.dto.request.BrandUpdateRequest;
import com.laptophub.brand.dto.response.BrandResponse;
import com.laptophub.brand.service.BrandService;
import com.laptophub.shared.response.ApiResponse;
import com.laptophub.shared.response.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/brands")
public class AdminBrandController {

    private final BrandService brandService;

    public AdminBrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BrandResponse>> create(
            @Valid @RequestBody BrandCreateRequest request
    ) {

        BrandResponse response =
                brandService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Thêm nhãn hàng thành công!",
                                response
                        )
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BrandResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody BrandUpdateRequest request
    ) {

        BrandResponse response =
                brandService.update(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Sửa lại nhãn hàng thành công!",
                        response
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BrandResponse>>> list(
            Pageable pageable
    ) {

        var page = brandService.list(pageable);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Lấy danh sách nhãn hàng thành công!",
                        PageResponse.of(page)
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BrandResponse>> getOne(
            @PathVariable Long id
    ) {

        BrandResponse response =
                brandService.getByIdOrThrow(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Lấy thông tin nhãn hàng thành công!",
                        response
                )
        );
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<BrandResponse>> activate(
            @PathVariable Long id
    ) {

        BrandResponse response =
                brandService.activate(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Kích hoạt nhãn hàng thành công!",
                        response
                )
        );
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<BrandResponse>> deactivate(
            @PathVariable Long id
    ) {

        BrandResponse response =
                brandService.deactivate(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Ẩn nhãn hàng thành công!",
                        response
                )
        );
    }
}