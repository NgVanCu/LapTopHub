package com.laptophub.product.service.impl;

import com.laptophub.brand.service.BrandService;
import com.laptophub.category.service.CategoryService;
import com.laptophub.product.dto.projection.ProductThumbnail;
import com.laptophub.product.dto.response.ComparisonItem;
import com.laptophub.product.dto.response.ComparisonResult;
import com.laptophub.product.dto.response.ComparisonSpecRow;
import com.laptophub.product.entity.Product;
import com.laptophub.product.entity.ProductSpecValue;
import com.laptophub.product.entity.ProductVariant;
import com.laptophub.product.entity.SpecificationDefinition;
import com.laptophub.product.enums.ProductStatus;
import com.laptophub.product.enums.ProductVariantStatus;
import com.laptophub.product.repository.ProductImageRepository;
import com.laptophub.product.repository.ProductRepository;
import com.laptophub.product.repository.ProductSpecValueRepository;
import com.laptophub.product.repository.ProductVariantRepository;
import com.laptophub.product.service.ProductComparisonService;
import com.laptophub.product.service.SpecificationDefinitionService;
import com.laptophub.shared.exception.AppException;
import com.laptophub.shared.exception.ErrorCode;
import com.laptophub.storage.service.ImageStorageService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductComparisonServiceImpl implements ProductComparisonService {
    private static final int MIN_ITEMS = 2;
    private static final int MAX_ITEMS = 3;

    private static final String GROUP_CONFIGURATION = "Cấu hình";

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductSpecValueRepository productSpecValueRepository;

    private final CategoryService categoryService;
    private final BrandService brandService;
    private final SpecificationDefinitionService specificationDefinitionService;

    private final ImageStorageService imageStorageService;

    public ProductComparisonServiceImpl(
            ProductVariantRepository productVariantRepository,
            ProductRepository productRepository,
            ProductImageRepository productImageRepository,
            ProductSpecValueRepository productSpecValueRepository,
            CategoryService categoryService,
            BrandService brandService,
            SpecificationDefinitionService specificationDefinitionService,
            ImageStorageService imageStorageService
    ) {
        this.productVariantRepository = productVariantRepository;
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.productSpecValueRepository = productSpecValueRepository;
        this.categoryService = categoryService;
        this.brandService = brandService;
        this.specificationDefinitionService = specificationDefinitionService;
        this.imageStorageService = imageStorageService;
    }

    public ComparisonResult compare(List<Long> requestedVariantIds) {

        // ---------------------------------------------------------
        // 1. Validate input
        // ---------------------------------------------------------

        if (requestedVariantIds == null || requestedVariantIds.isEmpty()) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Phải chọn từ " + MIN_ITEMS + " đến " + MAX_ITEMS + " sản phẩm để so sánh"
            );
        }

        /*
         * LinkedHashSet:
         * - loại ID trùng
         * - vẫn giữ nguyên thứ tự client gửi lên
         */
        List<Long> variantIds = new ArrayList<>(
                new LinkedHashSet<>(requestedVariantIds)
        );

        if (variantIds.size() < MIN_ITEMS || variantIds.size() > MAX_ITEMS) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Phải chọn từ " + MIN_ITEMS + " đến " + MAX_ITEMS + " sản phẩm để so sánh"
            );
        }

        // ---------------------------------------------------------
        // 2. Load variants - 1 query
        // ---------------------------------------------------------

        Map<Long, ProductVariant> variantsById =
                productVariantRepository.findAllById(variantIds)
                        .stream()
                        .collect(Collectors.toMap(
                                ProductVariant::getId,
                                Function.identity()
                        ));

        List<Long> unavailableVariantIds = variantIds.stream()
                .filter(id -> {
                    ProductVariant variant = variantsById.get(id);

                    return variant == null
                            || variant.getStatus() != ProductVariantStatus.ACTIVE;
                })
                .toList();

        if (!unavailableVariantIds.isEmpty()) {
            throw new AppException(
                    ErrorCode.PRODUCT_VARIANT_UNAVAILABLE,
                    "Sản phẩm không tồn tại hoặc không khả dụng: "
                            + unavailableVariantIds
            );
        }

        // ---------------------------------------------------------
        // 3. Lấy Product IDs từ variants
        // ---------------------------------------------------------

        List<Long> productIds = variantIds.stream()
                .map(id -> variantsById.get(id).getProductId())
                .distinct()
                .toList();

        // ---------------------------------------------------------
        // 4. Load products - 1 query
        // ---------------------------------------------------------

        Map<Long, Product> productsById =
                productRepository.findAllById(productIds)
                        .stream()
                        .collect(Collectors.toMap(
                                Product::getId,
                                Function.identity()
                        ));

        List<Long> unavailableProductIds = productIds.stream()
                .filter(id -> {
                    Product product = productsById.get(id);

                    return product == null
                            || product.getStatus() != ProductStatus.ACTIVE;
                })
                .toList();

        if (!unavailableProductIds.isEmpty()) {
            throw new AppException(
                    ErrorCode.PRODUCT_VARIANT_UNAVAILABLE,
                    "Sản phẩm không tồn tại hoặc không khả dụng: "
                            + unavailableProductIds
            );
        }

        // ---------------------------------------------------------
        // 5. Các product phải cùng category
        // ---------------------------------------------------------

        long categoryCount = productIds.stream()
                .map(id -> productsById.get(id).getCategoryId())
                .distinct()
                .count();

        if (categoryCount > 1) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Chỉ so sánh được các sản phẩm cùng danh mục"
            );
        }

        // ---------------------------------------------------------
        // 6. Batch load category names
        // ---------------------------------------------------------

        List<Long> categoryIds = productIds.stream()
                .map(id -> productsById.get(id).getCategoryId())
                .distinct()
                .toList();

        Map<Long, String> categoryNames =
                categoryService.findNamesByIds(categoryIds);

        // ---------------------------------------------------------
        // 7. Batch load brand names
        // ---------------------------------------------------------

        List<Long> brandIds = productIds.stream()
                .map(id -> productsById.get(id).getBrandId())
                .distinct()
                .toList();

        Map<Long, String> brandNames =
                brandService.findNamesByIds(brandIds);

        // ---------------------------------------------------------
        // 8. Batch load thumbnails
        // ---------------------------------------------------------

        Map<Long, ProductThumbnail> thumbnailsByProductId =
                productImageRepository
                        .findThumbnailsByProductIds(productIds)
                        .stream()
                        .collect(Collectors.toMap(
                                ProductThumbnail::getProductId,
                                Function.identity(),
                                (first, second) -> first
                        ));

        // ---------------------------------------------------------
        // 9. Build comparison items
        // ---------------------------------------------------------

        List<ComparisonItem> items = variantIds.stream()
                .map(variantId -> {

                    ProductVariant variant =
                            variantsById.get(variantId);

                    Product product =
                            productsById.get(variant.getProductId());

                    ProductThumbnail thumbnail =
                            thumbnailsByProductId.get(product.getId());

                    String thumbnailUrl = thumbnail == null
                            ? null
                            : imageStorageService.generateDownloadUrl(
                            thumbnail.getObjectKey()
                    );

                    return new ComparisonItem(
                            variant.getId(),
                            product.getId(),
                            product.getName(),
                            variant.getVariantName(),
                            variant.getSku(),
                            variant.getPrice(),
                            thumbnailUrl,
                            categoryNames.get(product.getCategoryId()),
                            brandNames.get(product.getBrandId())
                    );
                })
                .toList();

        // ---------------------------------------------------------
        // 10. Build specification comparison rows
        // ---------------------------------------------------------

        List<ComparisonSpecRow> specifications =
                buildSpecificationRows(
                        variantIds,
                        variantsById,
                        productIds
                );

        // ---------------------------------------------------------
        // 11. Final result
        // ---------------------------------------------------------

        return new ComparisonResult(
                items,
                specifications
        );
    }

    /**
     * Xây dựng toàn bộ dòng thông số dùng cho comparison.
     *
     * Nguồn dữ liệu gồm 2 phần:
     *
     * 1. Cấu hình trực tiếp trên ProductVariant:
     *    - RAM
     *    - Storage
     *    - Storage type
     *    - Color
     *
     * 2. Specification EAV của Product:
     *    - SpecificationDefinition
     *    - ProductSpecValue
     */
    private List<ComparisonSpecRow> buildSpecificationRows(
            List<Long> variantIds,
            Map<Long, ProductVariant> variantsById,
            List<Long> productIds
    ) {

        List<ComparisonSpecRow> rows = new ArrayList<>();

        // ---------------------------------------------------------
        // A. Cấu hình của Variant
        // ---------------------------------------------------------

        rows.add(
                configRow(
                        "ram",
                        "RAM",
                        "GB",
                        variantIds,
                        variantsById,
                        variant -> variant.getRamGb() == null
                                ? null
                                : String.valueOf(variant.getRamGb())
                )
        );

        rows.add(
                configRow(
                        "storage",
                        "Bộ nhớ trong",
                        "GB",
                        variantIds,
                        variantsById,
                        variant -> variant.getStorageGb() == null
                                ? null
                                : String.valueOf(variant.getStorageGb())
                )
        );

        rows.add(
                configRow(
                        "storage_type",
                        "Loại ổ cứng",
                        null,
                        variantIds,
                        variantsById,
                        ProductVariant::getStorageType
                )
        );

        rows.add(
                configRow(
                        "color",
                        "Màu sắc",
                        null,
                        variantIds,
                        variantsById,
                        ProductVariant::getColor
                )
        );

        // ---------------------------------------------------------
        // B. Specification EAV của Product
        // ---------------------------------------------------------

        List<ProductSpecValue> specValues =
                productSpecValueRepository.findByProductIdIn(productIds);

        if (specValues.isEmpty()) {
            return rows;
        }

        // ---------------------------------------------------------
        // C. Batch load SpecificationDefinition
        // ---------------------------------------------------------

        List<Long> definitionIds = specValues.stream()
                .map(ProductSpecValue::getSpecificationDefinitionId)
                .distinct()
                .toList();

        Map<Long, SpecificationDefinition> definitionsById =
                specificationDefinitionService.findByIds(definitionIds);

        // ---------------------------------------------------------
        // D. Map:
        // productId + definitionId -> value
        // ---------------------------------------------------------

        Map<String, String> valueByProductAndDefinition =
                specValues.stream()
                        .collect(Collectors.toMap(
                                value -> createSpecKey(
                                        value.getProductId(),
                                        value.getSpecificationDefinitionId()
                                ),
                                ProductSpecValue::getValue,
                                (first, second) -> first
                        ));

        // ---------------------------------------------------------
        // E. Sắp xếp SpecificationDefinition theo displayOrder
        // ---------------------------------------------------------

        LinkedHashMap<Long, SpecificationDefinition> orderedDefinitions =
                specValues.stream()
                        .map(ProductSpecValue::getSpecificationDefinitionId)
                        .distinct()
                        .map(definitionsById::get)
                        .filter(definition -> definition != null)
                        .sorted(
                                Comparator.comparingInt(
                                        SpecificationDefinition::getDisplayOrder
                                )
                        )
                        .collect(Collectors.toMap(
                                SpecificationDefinition::getId,
                                Function.identity(),
                                (first, second) -> first,
                                LinkedHashMap::new
                        ));

        // ---------------------------------------------------------
        // F. Build từng specification row
        // ---------------------------------------------------------

        for (SpecificationDefinition definition :
                orderedDefinitions.values()) {

            Map<Long, String> values = new LinkedHashMap<>();

            for (Long variantId : variantIds) {

                ProductVariant variant =
                        variantsById.get(variantId);

                Long productId =
                        variant.getProductId();

                String key = createSpecKey(
                        productId,
                        definition.getId()
                );

                values.put(
                        variantId,
                        valueByProductAndDefinition.get(key)
                );
            }

            rows.add(
                    new ComparisonSpecRow(
                            definition.getCode(),
                            definition.getLabel(),
                            definition.getUnit(),
                            definition.getGroupLabel(),
                            values
                    )
            );
        }

        return rows;
    }

    /**
     * Tạo key dùng để map:
     *
     * productId + specificationDefinitionId
     *
     * Ví dụ:
     *
     * 10:5
     */
    private String createSpecKey(
            Long productId,
            Long specificationDefinitionId
    ) {
        return productId + ":" + specificationDefinitionId;
    }

    /**
     * Tạo một row cho các field cấu hình trực tiếp trên ProductVariant.
     */
    private ComparisonSpecRow configRow(
            String code,
            String label,
            String unit,
            List<Long> variantIds,
            Map<Long, ProductVariant> variantsById,
            Function<ProductVariant, String> valueExtractor
    ) {

        Map<Long, String> values = new LinkedHashMap<>();

        for (Long variantId : variantIds) {

            ProductVariant variant =
                    variantsById.get(variantId);

            values.put(
                    variantId,
                    valueExtractor.apply(variant)
            );
        }

        return new ComparisonSpecRow(
                code,
                label,
                unit,
                GROUP_CONFIGURATION,
                values
        );
    }
}
