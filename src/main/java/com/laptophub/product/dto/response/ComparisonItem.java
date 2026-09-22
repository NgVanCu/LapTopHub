package com.laptophub.product.dto.response;

import java.math.BigDecimal;

public record ComparisonItem(
        Long variantId,
        Long productId,
        String productName,
        String variantName,
        String sku,
        BigDecimal price,
        String thumbnailUrl,
        String categoryName,
        String brandName) {
}
