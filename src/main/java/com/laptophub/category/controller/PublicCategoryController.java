package com.laptophub.category.controller;

import com.laptophub.category.dto.response.CategoryPublicResponse;
import com.laptophub.category.service.CategoryService;
import com.laptophub.shared.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/public/categories")
public class PublicCategoryController {
    private final CategoryService categoryService;

    public PublicCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryPublicResponse>>> list() {
        List<CategoryPublicResponse> responses = categoryService.listActive().stream()
                .map(CategoryPublicResponse::from)
                .toList();
        return ResponseEntity.ok(
                ApiResponse.success("oke",responses)
        );
    }
}
