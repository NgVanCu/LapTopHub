package com.laptophub.product.service;

import com.laptophub.product.dto.request.ProductImageCreateRequest;
import com.laptophub.product.dto.request.ProductImageReorderRequest;
import com.laptophub.product.dto.request.ProductImagesCreateRequest;
import com.laptophub.product.dto.response.ProductImageResponse;
import com.laptophub.product.entity.ProductImage;

import java.util.List;
import java.util.Map;

public interface ProductImageService {
    ProductImageResponse addImage(Long productId, ProductImageCreateRequest request);

    List<ProductImageResponse> addImages(
            Long productId,
            ProductImagesCreateRequest request
    );

    void deleteImage(Long productId, Long imageId);

    ProductImage getOwnedOrThrow(Long productId, Long imageId);

    List<ProductImageResponse> listByProduct(Long productId);

    List<ProductImageResponse> reorderImages(
            Long productId,
            ProductImageReorderRequest request
    );
    Map<Long, String> findThumbnailUrlsByProductIds(List<Long> productIds);
}
