package com.laptophub.order.dto.response;

import com.laptophub.order.entity.OrderItem;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long productVariantId,
        Long productId,
        String productSlug,
        String productName,
        String variantName,
        String sku,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal discountAmount,
        BigDecimal lineTotal) {

    public static OrderItemResponse from(OrderItem item, Long productId, String productSlug) {
        BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                .subtract(item.getDiscountAmount());
        return new OrderItemResponse(
                item.getId(),
                item.getProductVariantId(),
                productId,
                productSlug,
                item.getProductName(),
                item.getVariantName(),
                item.getSku(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getDiscountAmount(),
                lineTotal);
    }
}
