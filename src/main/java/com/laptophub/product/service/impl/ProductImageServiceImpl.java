package com.laptophub.product.service.impl;


import com.laptophub.product.dto.request.ProductImageCreateRequest;
import com.laptophub.product.dto.request.ProductImageReorderRequest;
import com.laptophub.product.dto.request.ProductImagesCreateRequest;
import com.laptophub.product.dto.response.ProductImageResponse;
import com.laptophub.product.entity.Product;
import com.laptophub.product.entity.ProductImage;
import com.laptophub.product.repository.ProductImageRepository;
import com.laptophub.product.service.ProductCacheService;
import com.laptophub.product.service.ProductImageService;
import com.laptophub.product.service.ProductService;
import com.laptophub.shared.exception.AppException;
import com.laptophub.shared.exception.ErrorCode;
import com.laptophub.storage.dto.request.ConfirmUploadRequest;
import com.laptophub.storage.enums.ImagePurpose;
import com.laptophub.storage.service.ImageStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductImageServiceImpl implements ProductImageService {
    private final ProductImageRepository productImageRepository;
    private final ProductService productService;
    private final ImageStorageService imageStorageService;
    private final ProductCacheService productCacheService;
    public ProductImageServiceImpl(ProductImageRepository productImageRepository, ProductService productService,
                                   ImageStorageService imageStorageService,
                                   ProductCacheService productCacheService) {
        this.productImageRepository = productImageRepository;
        this.productService = productService;
        this.imageStorageService = imageStorageService;
        this.productCacheService = productCacheService;
    }

    @Override
    @Transactional
    public ProductImageResponse addImage(Long productId, ProductImageCreateRequest request) {
        Product product = productService.getByIdOrThrow(productId);

        int sortOrder = nextSortOrder(productId);

        String permanentKey =
                imageStorageService.confirmUpload(
                        ImagePurpose.PRODUCT_IMAGE,
                        productId,
                        new ConfirmUploadRequest(request.objectKey())
                );
        ProductImage productImage =
                ProductImage.create(
                        product.getId(),
                        permanentKey,
                        request.altText(),
                        sortOrder
                );

        ProductImage saved =
                productImageRepository.save(productImage);
        productCacheService.evictProductDetail(product.getSlug());
        productCacheService.evictProductSearch();
        return toResponse(saved);
    }

    @Override
    @Transactional
    public List<ProductImageResponse> addImages(
            Long productId,
            ProductImagesCreateRequest request
    ) {
        Product product =
                productService.getByIdOrThrow(productId);

        int sortOrder = nextSortOrder(productId);

        List<ProductImage> images =
                new ArrayList<>();

        for (ProductImageCreateRequest imageRequest : request.images()) {

            String permanentKey =
                    imageStorageService.confirmUpload(
                            ImagePurpose.PRODUCT_IMAGE,
                            productId,
                            new ConfirmUploadRequest(
                                    imageRequest.objectKey()
                            )
                    );

            ProductImage image =
                    ProductImage.create(
                            product.getId(),
                            permanentKey,
                            imageRequest.altText(),
                            sortOrder++
                    );

            images.add(image);
        }

        List<ProductImage> savedImages =
                productImageRepository.saveAll(images);

        productCacheService.evictProductDetail(product.getSlug());
        productCacheService.evictProductSearch();
        return savedImages.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ProductImage getOwnedOrThrow(Long productId, Long imageId) {
        return productImageRepository.findByIdAndProductId(imageId, productId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Override
    public List<ProductImageResponse> listByProduct(Long productId) {
        productService.getByIdOrThrow(productId);
        return productImageRepository
                .findByProductIdOrderBySortOrderAsc(productId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public List<ProductImageResponse> reorderImages(
            Long productId,
            ProductImageReorderRequest request
    ) {
        List<Long> orderedIds = request.orderedImageIds();

        Product product = productService.getByIdOrThrow(productId);

        List<ProductImage> existingImages =
                productImageRepository
                        .findByProductIdOrderBySortOrderAsc(productId);

        // Số lượng phải giống nhau
        if (orderedIds.size() != existingImages.size()) {
            throw new AppException(
                    ErrorCode.INVALID_IMAGE_ORDER
            );
        }

        // ID không được trùng
        Set<Long> orderedIdSet =
                new HashSet<>(orderedIds);

        if (orderedIdSet.size() != orderedIds.size()) {
            throw new AppException(
                    ErrorCode.INVALID_IMAGE_ORDER
            );
        }

        // Map imageId -> ProductImage
        Map<Long, ProductImage> imageMap =
                existingImages.stream()
                        .collect(Collectors.toMap(
                                ProductImage::getId,
                                image -> image
                        ));

        // Tất cả ID client gửi phải thuộc Product
        if (!imageMap.keySet().equals(orderedIdSet)) {
            throw new AppException(
                    ErrorCode.INVALID_IMAGE_ORDER
            );
        }

        // Cập nhật lại thứ tự
        List<ProductImage> reordered =
                new ArrayList<>(orderedIds.size());

        for (int index = 0; index < orderedIds.size(); index++) {

            ProductImage image = imageMap.get(orderedIds.get(index));

            image.changeSortOrder(index);

            reordered.add(image);
        }
        productCacheService.evictProductDetail(product.getSlug());
        productCacheService.evictProductSearch();
        return reordered.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteImage(Long productId, Long imageId) {

        // 1. Verify ProductImage
        Product product =
                productService.getByIdOrThrow(productId);

        ProductImage image =
                getOwnedOrThrow(productId, imageId);

        String objectKey = image.getObjectKey();

        // 2. Delete MinIO object
        imageStorageService.delete(objectKey);

        // 3. Delete DB record
        productImageRepository.delete(image);

        // 4. Normalize sortOrder
        normalizeSortOrder(productId);
        productCacheService.evictProductDetail(product.getSlug());
        productCacheService.evictProductSearch();
    }

    private ProductImageResponse toResponse(ProductImage productImage) {

        String imageUrl =
                imageStorageService.generateDownloadUrl(
                        productImage.getObjectKey()
                );

        return ProductImageResponse.from(
                productImage,
                imageUrl
        );
    }

    private int nextSortOrder(Long productId) {
        return productImageRepository
                .findMaxSortOrderByProductId(productId)
                .map(max -> max + 1)
                .orElse(0);
    }

    private void normalizeSortOrder(Long productId) {

        List<ProductImage> images =
                productImageRepository
                        .findByProductIdOrderBySortOrderAsc(productId);

        for (int index = 0; index < images.size(); index++) {
            images.get(index).changeSortOrder(index);
        }
    }
}
