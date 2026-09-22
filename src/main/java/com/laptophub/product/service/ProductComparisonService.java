package com.laptophub.product.service;

import com.laptophub.product.dto.response.ComparisonResult;

import java.util.List;

public interface ProductComparisonService {
    ComparisonResult compare(List<Long> requestedVariantIds);
}
