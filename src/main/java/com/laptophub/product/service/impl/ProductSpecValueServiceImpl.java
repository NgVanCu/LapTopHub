package com.laptophub.product.service.impl;

import com.laptophub.product.dto.request.ProductSpecValuesUpsertRequest;
import com.laptophub.product.dto.response.ProductSpecValueResponse;
import com.laptophub.product.entity.Product;
import com.laptophub.product.entity.ProductSpecValue;
import com.laptophub.product.entity.SpecificationDefinition;
import com.laptophub.product.repository.ProductSpecValueRepository;
import com.laptophub.product.service.ProductCacheService;
import com.laptophub.product.service.ProductService;
import com.laptophub.product.service.ProductSpecValueService;
import com.laptophub.product.service.SpecificationDefinitionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductSpecValueServiceImpl
        implements ProductSpecValueService {

    private final ProductSpecValueRepository productSpecValueRepository;
    private final ProductService productService;
    private final SpecificationDefinitionService specificationDefinitionService;
    private final ProductCacheService productCacheService;
    public ProductSpecValueServiceImpl(
            ProductSpecValueRepository productSpecValueRepository,
            ProductService productService,
            SpecificationDefinitionService specificationDefinitionService,
            ProductCacheService productCacheService
    ) {
        this.productSpecValueRepository = productSpecValueRepository;
        this.productService = productService;
        this.specificationDefinitionService =
                specificationDefinitionService;
        this.productCacheService = productCacheService;
    }

    @Override
    @Transactional
    public List<ProductSpecValueResponse> upsertValues(
            Long productId,
            ProductSpecValuesUpsertRequest request
    ) {

        /*
         * Kiểm tra product tồn tại.
         */
        Product product =  productService.getByIdOrThrow(productId);

        /*
         * Lấy toàn bộ specificationDefinitionId
         * từ request.
         *
         * distinct() tránh duplicate ID không cần thiết.
         */
        List<Long> specificationDefinitionIds =
                request.values()
                        .stream()
                        .map(
                                ProductSpecValuesUpsertRequest
                                        .SpecValueItem
                                        ::specificationDefinitionId
                        )
                        .distinct()
                        .toList();

        /*
         * Batch lấy SpecificationDefinition.
         *
         * 1 query thay vì N lần findById().
         */
        Map<Long, SpecificationDefinition> definitionsById =
                specificationDefinitionService.findByIds(
                        specificationDefinitionIds
                );

        /*
         * Nếu có ID không tồn tại thì đi vào error path.
         *
         * Với dữ liệu hợp lệ, đoạn này không tạo thêm query.
         */
        for (Long specificationDefinitionId :
                specificationDefinitionIds) {

            if (!definitionsById.containsKey(
                    specificationDefinitionId
            )) {
                specificationDefinitionService.getByIdOrThrow(
                        specificationDefinitionId
                );
            }
        }

        /*
         * Request đại diện cho TOÀN BỘ specification
         * của product.
         */
        if (specificationDefinitionIds.isEmpty()) {

            productSpecValueRepository.deleteAll(
                    productSpecValueRepository.findByProductId(
                            productId
                    )
            );
            productCacheService.evictProductDetail(product.getSlug());
            return List.of();
        }

        /*
         * Xóa các specification cũ không còn trong request.
         */
        productSpecValueRepository
                .deleteByProductIdAndSpecificationDefinitionIdNotIn(
                        productId,
                        specificationDefinitionIds
                );

        /*
         * Lấy các giá trị hiện tại.
         *
         * 1 query.
         */
        Map<Long, ProductSpecValue> existingByDefinitionId =
                new HashMap<>();

        for (ProductSpecValue existing :
                productSpecValueRepository.findByProductId(
                        productId
                )) {

            existingByDefinitionId.put(
                    existing.getSpecificationDefinitionId(),
                    existing
            );
        }

        List<ProductSpecValueResponse> responses =
                new ArrayList<>(request.values().size());

        /*
         * Update hoặc insert.
         */
        for (ProductSpecValuesUpsertRequest.SpecValueItem item :
                request.values()) {

            ProductSpecValue value =
                    existingByDefinitionId.get(
                            item.specificationDefinitionId()
                    );

            if (value != null) {

                value.changeValue(item.value());

            } else {

                value = productSpecValueRepository.save(
                        ProductSpecValue.create(
                                productId,
                                item.specificationDefinitionId(),
                                item.value()
                        )
                );
            }

            responses.add(
                    ProductSpecValueResponse.from(
                            value,
                            definitionsById.get(
                                    item.specificationDefinitionId()
                            )
                    )
            );
        }
        productCacheService.evictProductDetail(product.getSlug());
        return responses;
    }

    @Override
    public List<ProductSpecValueResponse> listByProduct(
            Long productId
    ) {

        /*
         * Query 1:
         * Lấy toàn bộ spec value của product.
         */
        List<ProductSpecValue> values =
                productSpecValueRepository.findByProductId(
                        productId
                );

        if (values.isEmpty()) {
            return List.of();
        }

        /*
         * Lấy toàn bộ definition ID cần thiết.
         */
        List<Long> definitionIds =
                values.stream()
                        .map(
                                ProductSpecValue
                                        ::getSpecificationDefinitionId
                        )
                        .distinct()
                        .toList();

        /*
         * Query 2:
         * Batch lấy SpecificationDefinition.
         */
        Map<Long, SpecificationDefinition> definitionsById =
                specificationDefinitionService.findByIds(
                        definitionIds
                );

        /*
         * Java mapping.
         *
         * Không có query DB.
         */
        return values.stream()
                .map(value ->
                        ProductSpecValueResponse.from(
                                value,
                                definitionsById.get(
                                        value.getSpecificationDefinitionId()
                                )
                        )
                )
                .toList();
    }
}