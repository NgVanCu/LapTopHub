package com.laptophub.brand.dto.response;


public record BrandPublicResponse(
        Long id,
        String name,
        String slug,
        String logoUrl
) {

    public static BrandPublicResponse from(
            BrandResponse brand
    ) {
        return new BrandPublicResponse(
                brand.id(),
                brand.name(),
                brand.slug(),
                brand.logoUrl()
        );
    }
}
