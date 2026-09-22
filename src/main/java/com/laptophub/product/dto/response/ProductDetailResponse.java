package com.laptophub.product.dto.response;

import java.util.List;

public record ProductDetailResponse(
        Long id,
        String name,
        String slug,
        String categoryName,
        String brandName,
        String shortDescription,
        String description,
        List<ProductImageResponse> images,
        List<ProductVariantResponse> variants,
        List<ProductSpecValueResponse> specifications) {
}
