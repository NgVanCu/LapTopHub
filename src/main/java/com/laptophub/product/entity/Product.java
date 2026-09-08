package com.laptophub.product.entity;

import com.laptophub.product.enums.ProductStatus;
import com.laptophub.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "brand_id", nullable = false)
    private Long brandId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "slug", nullable = false, length = 280, unique = true)
    private String slug;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProductStatus status;

    private Product(Long categoryId, Long brandId, String name, String slug, String shortDescription,
                    String description) {
        this.categoryId = Objects.requireNonNull(categoryId, "categoryId không được để trống");
        this.brandId = Objects.requireNonNull(brandId, "brandId không được để trống");
        this.name = Objects.requireNonNull(name, "name không được để trống");
        this.slug = Objects.requireNonNull(slug, "slug không được để trống");
        this.shortDescription = shortDescription;
        this.description = description;
        this.status = ProductStatus.ACTIVE;
    }

    public static Product create(Long categoryId, Long brandId, String name, String slug, String shortDescription,
                                 String description) {
        return new Product(categoryId, brandId, name, slug, shortDescription, description);
    }

    public void update(String name, String slug, String shortDescription, String description) {
        this.name = Objects.requireNonNull(name, "name không được để trống");
        this.slug = Objects.requireNonNull(slug, "slug không được để trống");
        this.shortDescription = shortDescription;
        this.description = description;
    }

    public void changeCategory(Long categoryId) {
        this.categoryId = Objects.requireNonNull(categoryId, "categoryId không được để trống");
    }

    public void changeBrand(Long brandId) {
        this.brandId = Objects.requireNonNull(brandId, "brandId không được để trống");
    }

    public void activate() {
        this.status = ProductStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = ProductStatus.INACTIVE;
    }
}
