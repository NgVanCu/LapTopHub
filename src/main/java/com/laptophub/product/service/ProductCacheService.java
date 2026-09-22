package com.laptophub.product.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
public class ProductCacheService {

    @CacheEvict(
            cacheNames = "product-detail",
            key = "#slug"
    )
    public void evictProductDetail(String slug) {
    }

    @CacheEvict(
            cacheNames = "product-detail",
            allEntries = true
    )
    public void evictAllProductDetail() {
    }

    @CacheEvict(
            cacheNames = "product-search",
            allEntries = true
    )
    public void evictProductSearch() {
    }
}