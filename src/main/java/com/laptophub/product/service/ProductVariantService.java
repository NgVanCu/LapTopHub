package com.laptophub.product.service;

import com.laptophub.product.dto.request.ProductVariantCreateRequest;
import com.laptophub.product.dto.request.ProductVariantUpdateRequest;
import com.laptophub.product.entity.ProductVariant;

import java.util.List;

public interface ProductVariantService {

    ProductVariant addVariant(Long productId, ProductVariantCreateRequest request);

    ProductVariant updateVariant(Long productId, Long variantId, ProductVariantUpdateRequest request);

    ProductVariant getOwnedOrThrow(Long productId, Long variantId);

    List<ProductVariant> listByProduct(Long productId);

    ProductVariant activate(Long productId, Long variantId);

    ProductVariant deactivate(Long productId, Long variantId);

    ProductVariant getByIdOrThrow(Long variantId);
}
