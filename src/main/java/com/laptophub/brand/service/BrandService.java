package com.laptophub.brand.service;

import com.laptophub.brand.dto.request.BrandCreateRequest;
import com.laptophub.brand.dto.request.BrandUpdateRequest;
import com.laptophub.brand.dto.response.BrandResponse;
import com.laptophub.brand.entity.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BrandService {
    BrandResponse create(BrandCreateRequest request);

    BrandResponse update(Long id, BrandUpdateRequest request);

    BrandResponse getByIdOrThrow(Long id);

    Page<BrandResponse> list(Pageable pageable);

    List<BrandResponse> listActive();

    BrandResponse activate(Long id);

    BrandResponse deactivate(Long id);
}
