package com.laptophub.product.entity;

import com.laptophub.product.enums.ProductVariantStatus;
import com.laptophub.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "product_variants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductVariant extends BaseEntity {
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "sku", nullable = false, length = 100, unique = true)
    private String sku;

    @Column(name = "variant_name", length = 255)
    private String variantName;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "ram_gb")
    private Integer ramGb;

    @Column(name = "storage_gb")
    private Integer storageGb;

    @Column(name = "storage_type", length = 20)
    private String storageType;

    @Column(name = "color", length = 50)
    private String color;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProductVariantStatus status;

    private ProductVariant(Long productId, String sku, String variantName, BigDecimal price, Integer ramGb,
                           Integer storageGb, String storageType, String color) {
        this.productId = Objects.requireNonNull(productId, "productId không được để trống");
        this.sku = Objects.requireNonNull(sku, "sku không được để trống");
        this.variantName = variantName;
        this.price = Objects.requireNonNull(price, "price không được để trống");
        this.ramGb = ramGb;
        this.storageGb = storageGb;
        this.storageType = storageType;
        this.color = color;
        this.status = ProductVariantStatus.ACTIVE;
    }

    public static ProductVariant create(Long productId, String sku, String variantName, BigDecimal price,
                                        Integer ramGb, Integer storageGb, String storageType, String color) {
        return new ProductVariant(productId, sku, variantName, price, ramGb, storageGb, storageType, color);
    }

    public void update(String variantName, BigDecimal price, Integer ramGb, Integer storageGb, String storageType,
                       String color) {
        this.variantName = variantName;
        this.price = Objects.requireNonNull(price, "price không được để trống");
        this.ramGb = ramGb;
        this.storageGb = storageGb;
        this.storageType = storageType;
        this.color = color;
    }

    public void activate() {
        this.status = ProductVariantStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = ProductVariantStatus.INACTIVE;
    }
}
