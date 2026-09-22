package com.laptophub.product.service.impl;

import com.laptophub.brand.service.BrandService;
import com.laptophub.category.service.CategoryService;
import com.laptophub.product.dto.projection.ProductSearchProjection;
import com.laptophub.product.dto.projection.ProductThumbnail;
import com.laptophub.product.dto.response.*;
import com.laptophub.product.entity.Product;
import com.laptophub.product.entity.ProductImage;
import com.laptophub.product.enums.ProductSortOption;
import com.laptophub.product.enums.ProductVariantStatus;
import com.laptophub.product.repository.ProductImageRepository;
import com.laptophub.product.repository.ProductRepository;
import com.laptophub.product.repository.ProductVariantRepository;
import com.laptophub.product.service.ProductSearchService;
import com.laptophub.product.service.ProductSpecValueService;
import com.laptophub.shared.exception.AppException;
import com.laptophub.shared.exception.ErrorCode;
import com.laptophub.shared.response.PageResponse;
import com.laptophub.storage.service.ImageStorageService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductSearchServiceImpl implements ProductSearchService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductImageRepository productImageRepository;

    private final CategoryService categoryService;
    private final BrandService brandService;
    private final ProductSpecValueService productSpecValueService;
    private final ImageStorageService imageStorageService;

    public ProductSearchServiceImpl(
            ProductRepository productRepository,
            ProductVariantRepository productVariantRepository,
            ProductImageRepository productImageRepository,
            CategoryService categoryService,
            BrandService brandService,
            ProductSpecValueService productSpecValueService,
            ImageStorageService imageStorageService
    ) {
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.productImageRepository = productImageRepository;
        this.categoryService = categoryService;
        this.brandService = brandService;
        this.productSpecValueService = productSpecValueService;
        this.imageStorageService = imageStorageService;
    }

    @Override
    @Cacheable(
            cacheNames = "product-search",
            keyGenerator = "productSearchCacheKeyGenerator"
    )
    public PageResponse<ProductListItemResponse> search(
            Long categoryId,
            Long brandId,
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            ProductSortOption sort,
            Pageable pageable
    ) {
        System.out.println(">>> SEARCH DATABASE");
        Pageable sortedPageable =
                applySort(pageable, sort);

        /*
         * Query 1:
         *
         * Product + active variant MIN(price) + MAX(price)
         */
        Page<ProductSearchProjection> productPage =
                productRepository.searchPublic(
                        categoryId,
                        brandId,
                        keyword,
                        minPrice,
                        maxPrice,
                        sortedPageable
                );

        if (productPage.isEmpty()) {
            Page<ProductListItemResponse> empty = new PageImpl<>(
                    List.of(),
                    sortedPageable,
                    productPage.getTotalElements()
            );
            return PageResponse.of(empty);
        }

        List<ProductSearchProjection> products =
                productPage.getContent();

        List<Long> productIds =
                products.stream()
                        .map(ProductSearchProjection::getId)
                        .toList();

        /*
         * Query 2:
         * Batch category names.
         */
        Map<Long, String> categoryNames =
                categoryService.findNamesByIds(
                        products.stream()
                                .map(ProductSearchProjection::getCategoryId)
                                .distinct()
                                .toList()
                );

        /*
         * Query 3:
         * Batch brand names.
         */
        Map<Long, String> brandNames =
                brandService.findNamesByIds(
                        products.stream()
                                .map(ProductSearchProjection::getBrandId)
                                .distinct()
                                .toList()
                );

        /*
         * Query 4:
         * Batch thumbnail.
         */
        Map<Long, ProductThumbnail> thumbnailsByProductId =
                productImageRepository
                        .findThumbnailsByProductIds(productIds)
                        .stream()
                        .collect(Collectors.toMap(
                                ProductThumbnail::getProductId,
                                thumbnail -> thumbnail,
                                (first, second) -> first
                        ));

        /*
         * Mapping hoàn toàn ở Java.
         * Không query DB thêm.
         */
        List<ProductListItemResponse> content =
                products.stream()
                        .map(product -> {

                            ProductThumbnail thumbnail =
                                    thumbnailsByProductId.get(
                                            product.getId()
                                    );

                            String thumbnailUrl = null;

                            if (thumbnail != null) {
                                thumbnailUrl =
                                        imageStorageService
                                                .generateDownloadUrl(
                                                        thumbnail.getObjectKey()
                                                );
                            }

                            return new ProductListItemResponse(
                                    product.getId(),
                                    product.getName(),
                                    product.getSlug(),
                                    categoryNames.get(
                                            product.getCategoryId()
                                    ),
                                    brandNames.get(
                                            product.getBrandId()
                                    ),
                                    product.getPriceFrom(),
                                    product.getPriceTo(),
                                    thumbnailUrl
                            );
                        })
                        .toList();

        Page<ProductListItemResponse> page = new PageImpl<>(
                content,
                sortedPageable,
                productPage.getTotalElements()
        );

        return PageResponse.of(page);
    }

    @Override
    @Cacheable(
            cacheNames = "product-detail",
            key = "#slug"
    )
    public ProductDetailResponse getDetailBySlug(String slug) {

        /*
         * Query 1:
         * Product public.
         */
        System.out.println(">>> QUERY DATABASE");
        Product product =
                productRepository
                        .findPublicBySlug(slug)
                        .orElseThrow(() ->
                                new AppException(
                                        ErrorCode.RESOURCE_NOT_FOUND
                                )
                        );

        /*
         * Query 2:
         * Category.
         */
        String categoryName =
                categoryService
                        .getByIdOrThrow(
                                product.getCategoryId()
                        )
                        .getName();

        /*
         * Query 3:
         * Brand.
         */
        String brandName =
                brandService
                        .getByIdOrThrow(
                                product.getBrandId()
                        )
                        .name();

        /*
         * Query 4:
         * Active variants.
         */
        List<ProductVariantResponse> variants =
                productVariantRepository
                        .findByProductIdAndStatus(
                                product.getId(),
                                ProductVariantStatus.ACTIVE
                        )
                        .stream()
                        .map(ProductVariantResponse::from)
                        .toList();

        /*
         * Query 5:
         * Images.
         */
        List<ProductImageResponse> images =
                productImageRepository
                        .findByProductIdOrderBySortOrderAsc(
                                product.getId()
                        )
                        .stream()
                        .map(this::toImageResponse)
                        .toList();

        /*
         * Query 6 + 7:
         * ProductSpecValue + SpecificationDefinition batch.
         */
        List<ProductSpecValueResponse> specifications =
                productSpecValueService.listByProduct(
                        product.getId()
                );

        return new ProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                categoryName,
                brandName,
                product.getShortDescription(),
                product.getDescription(),
                images,
                variants,
                specifications
        );
    }

    private ProductImageResponse toImageResponse(
            ProductImage image
    ) {
        String imageUrl =
                imageStorageService.generateDownloadUrl(
                        image.getObjectKey()
                );

        return ProductImageResponse.from(
                image,
                imageUrl
        );
    }

    private Pageable applySort(
            Pageable pageable,
            ProductSortOption sort
    ) {

        Sort sortOrder = switch (sort) {

            case NEWEST ->
                    Sort.by(
                            Sort.Order.desc("createdAt"),
                            Sort.Order.desc("id")
                    );

            case NAME_ASC ->
                    Sort.by(
                            Sort.Order.asc("name"),
                            Sort.Order.asc("id")
                    );

            case NAME_DESC ->
                    Sort.by(
                            Sort.Order.desc("name"),
                            Sort.Order.desc("id")
                    );

            case PRICE_ASC ->
                    Sort.by(
                            Sort.Order.asc("priceFrom"),
                            Sort.Order.asc("id")
                    );

            case PRICE_DESC ->
                    Sort.by(
                            Sort.Order.desc("priceFrom"),
                            Sort.Order.desc("id")
                    );
        };

        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sortOrder
        );
    }
}