package com.laptophub.product.repository;

import com.laptophub.product.dto.projection.ProductThumbnail;
import com.laptophub.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository
        extends JpaRepository<ProductImage, Long> {

    @Query("""
            SELECT MAX(pi.sortOrder)
            FROM ProductImage pi
            WHERE pi.productId = :productId
            """)
    Optional<Integer> findMaxSortOrderByProductId(
            @Param("productId") Long productId
    );

    Optional<ProductImage> findByIdAndProductId(
            Long id,
            Long productId
    );

    List<ProductImage> findByProductIdOrderBySortOrderAsc(
            Long productId
    );

    /*
     * Batch lấy toàn bộ image của nhiều product.
     *
     * Không sử dụng trong product listing hiện tại,
     * nhưng giữ lại vì có thể phục vụ comparison/batch API.
     */
    List<ProductImage> findByProductIdInOrderBySortOrderAsc(
            List<Long> productIds
    );

    /*
     * Batch lấy thumbnail cho nhiều product.
     *
     * Thumbnail = image có sortOrder nhỏ nhất của từng product.
     */
    @Query("""
            SELECT
                pi.productId AS productId,
                pi.objectKey AS objectKey
            FROM ProductImage pi
            WHERE pi.productId IN :productIds
              AND pi.sortOrder = (
                  SELECT MIN(pi2.sortOrder)
                  FROM ProductImage pi2
                  WHERE pi2.productId = pi.productId
              )
            """)
    List<ProductThumbnail> findThumbnailsByProductIds(
            @Param("productIds") List<Long> productIds
    );
}