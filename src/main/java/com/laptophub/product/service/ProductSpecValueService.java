package com.laptophub.product.service;

import com.laptophub.product.dto.request.ProductSpecValuesUpsertRequest;
import com.laptophub.product.dto.response.ProductSpecValueResponse;

import java.util.List;

public interface ProductSpecValueService {
    List<ProductSpecValueResponse> upsertValues(Long productId, ProductSpecValuesUpsertRequest request) ;

    List<ProductSpecValueResponse> listByProduct(Long productId);
}
