package com.laptophub.product.service.impl;

import com.laptophub.product.dto.request.ProductVariantCreateRequest;
import com.laptophub.product.dto.request.ProductVariantUpdateRequest;
import com.laptophub.product.entity.Product;
import com.laptophub.product.entity.ProductVariant;
import com.laptophub.product.repository.ProductVariantRepository;
import com.laptophub.product.service.ProductCacheService;
import com.laptophub.product.service.ProductService;
import com.laptophub.product.service.ProductVariantService;
import com.laptophub.shared.exception.AppException;
import com.laptophub.shared.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductVariantServiceImpl implements ProductVariantService {
    private final ProductVariantRepository productVariantRepository;
    private final ProductService productService;
    private final ProductCacheService productCacheService;
    public ProductVariantServiceImpl(ProductVariantRepository productVariantRepository,
                                     ProductService productService,  ProductCacheService productCacheService) {
        this.productVariantRepository = productVariantRepository;
        this.productService = productService;
        this.productCacheService = productCacheService;
    }

    @Override
    @Transactional
    public ProductVariant addVariant(Long productId, ProductVariantCreateRequest request) {
        Product product = productService.getByIdOrThrow(productId);
        if (productVariantRepository.existsBySku(request.sku())) {
            throw new AppException(ErrorCode.RESOURCE_CONFLICT, "SKU đã tồn tại");
        }
        ProductVariant variant = ProductVariant.create(productId, request.sku(), request.variantName(),
                request.price(), request.ramGb(), request.storageGb(), request.storageType(), request.color());
        ProductVariant saved = productVariantRepository.save(variant);
        productCacheService.evictProductDetail(product.getSlug());
        productCacheService.evictProductSearch();
        return saved;
    }

    @Override
    @Transactional
    public ProductVariant updateVariant(Long productId, Long variantId, ProductVariantUpdateRequest request) {
        ProductVariant variant = getOwnedOrThrow(productId, variantId);
        variant.update(request.variantName(), request.price(), request.ramGb(), request.storageGb(),
                request.storageType(), request.color());
        Product product = productService.getByIdOrThrow(productId);
        productCacheService.evictProductDetail(product.getSlug());
        productCacheService.evictProductSearch();
        return variant;
    }

    @Override
    public ProductVariant getOwnedOrThrow(Long productId, Long variantId) {
        return productVariantRepository.findByIdAndProductId(variantId, productId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Override
    public List<ProductVariant> listByProduct(Long productId) {
        return productVariantRepository.findByProductId(productId);
    }

    @Override
    @Transactional
    public ProductVariant activate(Long productId, Long variantId) {
        ProductVariant variant = getOwnedOrThrow(productId, variantId);
        variant.activate();
        Product product = productService.getByIdOrThrow(productId);
        productCacheService.evictProductDetail(product.getSlug());
        productCacheService.evictProductSearch();
        return variant;
    }

    @Override
    @Transactional
    public ProductVariant deactivate(Long productId, Long variantId) {
        ProductVariant variant = getOwnedOrThrow(productId, variantId);
        variant.deactivate();
        Product product = productService.getByIdOrThrow(productId);
        productCacheService.evictProductDetail(product.getSlug());
        productCacheService.evictProductSearch();
        return variant;
    }

    @Override
    public ProductVariant getByIdOrThrow(Long variantId) {
        return productVariantRepository.findById(variantId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }
}
