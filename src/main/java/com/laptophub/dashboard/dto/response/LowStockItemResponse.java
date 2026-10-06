package com.laptophub.dashboard.dto.response;

import com.laptophub.product.enums.ProductVariantStatus;

public record LowStockItemResponse(
        Long variantId,
        Long productId,
        String productName,
        String variantName,
        String sku,
        int onHandQuantity,
        int reservedQuantity,
        int availableQuantity,
        ProductVariantStatus status) {
}