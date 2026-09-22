package com.laptophub.product.dto.response;

import com.laptophub.product.entity.Product;
import com.laptophub.product.enums.ProductStatus;

import java.time.Instant;

public record ProductSummaryResponse(
        Long id,
        String name,
        String slug,
        String categoryName,
        String brandName,
        ProductStatus status,
        Instant createdAt) {

    public static ProductSummaryResponse from(Product product, String categoryName, String brandName) {
        return new ProductSummaryResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                categoryName,
                brandName,
                product.getStatus(),
                product.getCreatedAt());
    }
}

