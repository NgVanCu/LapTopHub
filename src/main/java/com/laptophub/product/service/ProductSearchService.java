package com.laptophub.product.service;

import com.laptophub.product.dto.response.ProductDetailResponse;
import com.laptophub.product.dto.response.ProductListItemResponse;
import com.laptophub.product.enums.ProductSortOption;
import com.laptophub.shared.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ProductSearchService {
    PageResponse<ProductListItemResponse> search(
            Long categoryId,
            Long brandId,
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            ProductSortOption sort,
            Pageable pageable
    );

    ProductDetailResponse getDetailBySlug(String slug);
}
