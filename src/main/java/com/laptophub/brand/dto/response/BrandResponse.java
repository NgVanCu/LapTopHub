package com.laptophub.brand.dto.response;

import com.laptophub.brand.entity.Brand;
import com.laptophub.brand.enums.BrandStatus;

import java.time.Instant;

public record BrandResponse(
        Long id,
        String name,
        String slug,
        String description,
        String logoUrl,
        BrandStatus status,
        Instant createdAt,
        Instant updatedAt) {

    public static BrandResponse from(Brand brand, String logoUrl) {
        return new BrandResponse(
                brand.getId(),
                brand.getName(),
                brand.getSlug(),
                brand.getDescription(),
                logoUrl,
                brand.getStatus(),
                brand.getCreatedAt(),
                brand.getUpdatedAt());
    }
}
