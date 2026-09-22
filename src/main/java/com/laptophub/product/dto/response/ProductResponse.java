package com.laptophub.product.dto.response;

import com.laptophub.product.entity.Product;
import com.laptophub.product.enums.ProductStatus;
import java.time.Instant;

public record ProductResponse(
        Long id,
        Long categoryId,
        String categoryName,
        Long brandId,
        String brandName,
        String name,
        String slug,
        String shortDescription,
        String description,
        ProductStatus status,
        Instant createdAt,
        Instant updatedAt) {

    public static ProductResponse from(Product product, String categoryName, String brandName) {
        return new ProductResponse(
                product.getId(),
                product.getCategoryId(),
                categoryName,
                product.getBrandId(),
                brandName,
                product.getName(),
                product.getSlug(),
                product.getShortDescription(),
                product.getDescription(),
                product.getStatus(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }
}
