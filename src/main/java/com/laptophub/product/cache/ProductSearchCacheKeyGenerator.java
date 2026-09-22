package com.laptophub.product.cache;

import com.laptophub.product.enums.ProductSortOption;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.math.BigDecimal;

@Component("productSearchCacheKeyGenerator")
public class ProductSearchCacheKeyGenerator implements KeyGenerator {

    @Override
    public Object generate(
            Object target,
            Method method,
            Object... params
    ) {

        Long categoryId = (Long) params[0];
        Long brandId = (Long) params[1];
        String keyword = (String) params[2];
        BigDecimal minPrice = (BigDecimal) params[3];
        BigDecimal maxPrice = (BigDecimal) params[4];
        ProductSortOption sort = (ProductSortOption) params[5];
        Pageable pageable = (Pageable) params[6];

        ProductSearchCacheKey cacheKey =
                new ProductSearchCacheKey(
                        categoryId,
                        brandId,
                        keyword,
                        minPrice,
                        maxPrice,
                        sort,
                        pageable.getPageNumber(),
                        pageable.getPageSize()
                );

        return "category=" + cacheKey.categoryId()
                + "|brand=" + cacheKey.brandId()
                + "|keyword=" + cacheKey.keyword()
                + "|minPrice=" + cacheKey.minPrice()
                + "|maxPrice=" + cacheKey.maxPrice()
                + "|sort=" + cacheKey.sort()
                + "|page=" + cacheKey.page()
                + "|size=" + cacheKey.size();
    }
}