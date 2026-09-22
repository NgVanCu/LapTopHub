package com.laptophub.product.controller;

import com.laptophub.product.dto.response.ComparisonResult;
import com.laptophub.product.service.ProductComparisonService;
import com.laptophub.shared.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/public/products")
public class PublicProductComparisonController {
    private final ProductComparisonService productComparisonService;

    public PublicProductComparisonController(ProductComparisonService productComparisonService) {
        this.productComparisonService = productComparisonService;
    }

    @GetMapping("/compare")
    public ResponseEntity<ApiResponse<ComparisonResult>> compare(@RequestParam List<Long> variantIds) {
        return ResponseEntity.ok(ApiResponse.success("Lấy kết quả so sánh thành công",productComparisonService.compare(variantIds)));
    }
}
