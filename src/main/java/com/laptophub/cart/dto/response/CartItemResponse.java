package com.laptophub.cart.dto.response;

import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        Long productVariantId,
        Long productId,
        String productName,
        String thumbnailUrl,
        String sku,
        String variantName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal lineTotal) {

    public static CartItemResponse from(CartLine line, String productName, String thumbnailUrl) {
        BigDecimal unitPrice = line.variant().getPrice();
        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(line.item().getQuantity()));
        return new CartItemResponse(
                line.item().getId(),
                line.variant().getId(),
                line.variant().getProductId(),
                productName,
                thumbnailUrl,
                line.variant().getSku(),
                line.variant().getVariantName(),
                unitPrice,
                line.item().getQuantity(),
                lineTotal);
    }
}
