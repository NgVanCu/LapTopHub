package com.laptophub.product.service;

import com.laptophub.product.dto.request.ProductCreateRequest;
import com.laptophub.product.dto.request.ProductUpdateRequest;
import com.laptophub.product.dto.response.ProductSummaryResponse;
import com.laptophub.product.entity.Product;
import com.laptophub.product.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface ProductService {
    Product create(ProductCreateRequest request);

    Product update(Long id, ProductUpdateRequest request);

    Product getByIdOrThrow(Long id);
    Page<ProductSummaryResponse> listAdmin(Long categoryId, Long brandId, ProductStatus status,
                                           String keyword, Pageable pageable);

    Product activate(Long id);

    Product deactivate(Long id);

    Map<Long, Product> findByIds(List<Long> ids);
}
