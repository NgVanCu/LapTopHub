package com.laptophub.product.controller;

import com.laptophub.product.dto.response.ProductDetailResponse;
import com.laptophub.product.dto.response.ProductListItemResponse;
import com.laptophub.product.enums.ProductSortOption;
import com.laptophub.product.service.ProductSearchService;
import com.laptophub.shared.response.ApiResponse;
import com.laptophub.shared.response.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/public/products")
public class PublicProductController {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final ProductSearchService productSearchService;

    public PublicProductController(ProductSearchService productSearchService) {
        this.productSearchService = productSearchService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductListItemResponse>>> search(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "NEWEST") ProductSortOption sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int size) {

        // Chuẩn hóa keyword trước khi truyền xuống service.
        String normalizedKeyword =
                keyword == null || keyword.isBlank()
                        ? null
                        : keyword.trim().toLowerCase();

        // Bảo vệ pagination:
        // page < 0       -> 0
        // size < 1       -> 1
        // size > 100     -> 100
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.clamp(size, 1, MAX_PAGE_SIZE);

        Pageable pageable = PageRequest.of(
                normalizedPage,
                normalizedSize
        );

        PageResponse<ProductListItemResponse> result = productSearchService.search(
                categoryId,
                brandId,
                normalizedKeyword,
                minPrice,
                maxPrice,
                sort,
                pageable
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "",
                        result
                )
        );
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getDetail(
            @PathVariable String slug) {

        ProductDetailResponse result =
                productSearchService.getDetailBySlug(slug);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "",
                        result
                )
        );
    }
}