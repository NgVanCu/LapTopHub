package com.laptophub.product.dto.response;

import java.math.BigDecimal;

public record ProductListItemResponse(
        Long id,
        String name,
        String slug,
        String categoryName,
        String brandName,
        BigDecimal priceFrom,
        BigDecimal priceTo,
        String thumbnailUrl) {
}
