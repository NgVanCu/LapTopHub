package com.laptophub.category.controller;

import com.laptophub.category.dto.request.CategoryCreateRequest;
import com.laptophub.category.dto.request.CategoryUpdateRequest;
import com.laptophub.category.dto.response.CategoryResponse;
import com.laptophub.category.service.CategoryService;
import com.laptophub.shared.response.ApiResponse;
import com.laptophub.shared.response.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/categories")
public class AdminCategoryController {
    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> create(@Valid @RequestBody CategoryCreateRequest request) {
        var category = categoryService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Thêm danh mục thành công!",
                                CategoryResponse.from(category)
                        )
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(@PathVariable Long id,
                                                                @Valid @RequestBody CategoryUpdateRequest request) {
        var category = categoryService.update(id, request);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Sửa lại danh mục thành công!",
                        CategoryResponse.from(category)
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CategoryResponse>>> list(Pageable pageable) {
        var page = categoryService.list(pageable).map(CategoryResponse::from);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Lấy danh sách danh mục thành công!",
                        PageResponse.of(page)
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getOne(@PathVariable Long id) {
        var category = categoryService.getByIdOrThrow(id);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Lấy thông tin danh mục thành công!",
                        CategoryResponse.from(category)
                )
        );
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<CategoryResponse>> activate(@PathVariable Long id) {
        var category = categoryService.activate(id);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Kích hoạt danh mục thành công!",
                        CategoryResponse.from(category)
                )
        );
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<CategoryResponse>> deactivate(@PathVariable Long id) {
        var category = categoryService.deactivate(id);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Ẩn danh mục thành công!",
                        CategoryResponse.from(category)
                )
        );
    }
}
