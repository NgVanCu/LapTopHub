package com.laptophub.product.repository;

import com.laptophub.product.entity.ProductVariant;
import com.laptophub.product.enums.ProductVariantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository
        extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> findByProductId(
            Long productId
    );

    List<ProductVariant> findByProductIdAndStatus(
            Long productId,
            ProductVariantStatus status
    );

    /*
     * Batch lấy variants của nhiều product.
     *
     * Hiện tại chưa dùng trong ProductSearchServiceImpl,
     * nhưng giữ lại để phục vụ các API batch/compare sau này.
     */
    List<ProductVariant> findByProductIdInAndStatus(
            List<Long> productIds,
            ProductVariantStatus status
    );

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(
            String sku,
            Long id
    );

    Optional<ProductVariant> findByIdAndProductId(
            Long id,
            Long productId
    );
}