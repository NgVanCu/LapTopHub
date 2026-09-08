package com.laptophub.product.entity;

import com.laptophub.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Table(name = "product_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductImage extends BaseEntity {
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "object_key", nullable = false, length = 500)
    private String objectKey;

    @Column(name = "alt_text", length = 255)
    private String altText;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    private ProductImage(
            Long productId,
            String objectKey,
            String altText,
            int sortOrder
    ) {
        this.productId = Objects.requireNonNull(
                productId,
                "productId không được để trống"
        );

        this.objectKey = Objects.requireNonNull(
                objectKey,
                "objectKey không được để trống"
        );

        this.altText = altText;
        this.sortOrder = sortOrder;
    }

    public static ProductImage create(
            Long productId,
            String objectKey,
            String altText,
            int sortOrder
    ) {
        return new ProductImage(
                productId,
                objectKey,
                altText,
                sortOrder
        );
    }

    public void changeSortOrder(int sortOrder) {
        if (sortOrder < 0) {
            throw new IllegalArgumentException(
                    "sortOrder must not be negative"
            );
        }
        this.sortOrder = sortOrder;
    }

    public void changeAltText(String altText) {
        this.altText = altText;
    }
}
