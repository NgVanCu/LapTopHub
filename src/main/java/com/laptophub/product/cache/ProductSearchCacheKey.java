package com.laptophub.product.cache;

import com.laptophub.product.enums.ProductSortOption;

import java.math.BigDecimal;

public record ProductSearchCacheKey(
        Long categoryId,
        Long brandId,
        String keyword,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        ProductSortOption sort,
        int page,
        int size
) {
}