package com.laptophub.dashboard.dto.response;

import java.math.BigDecimal;

public record BestSellingItemResponse(
        Long variantId,
        Long productId,
        String productName,
        String variantName,
        String sku,
        long quantitySold,
        BigDecimal revenue) {
}

