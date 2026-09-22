package com.laptophub.product.dto.response;

import com.laptophub.product.entity.ProductImage;

public record ProductImageResponse(Long id, String productUrl, String altText, int sortOrder) {

    public static ProductImageResponse from(ProductImage image, String productUrl) {
        return new ProductImageResponse(image.getId(), productUrl, image.getAltText(), image.getSortOrder());
    }
}