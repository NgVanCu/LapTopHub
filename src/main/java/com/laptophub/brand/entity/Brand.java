package com.laptophub.brand.entity;

import com.laptophub.brand.enums.BrandStatus;
import com.laptophub.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Table(name = "brands")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Brand extends BaseEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "slug", nullable = false, length = 160, unique = true)
    private String slug;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "logo_key", length = 500)
    private String logoKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BrandStatus status;

    private Brand(String name, String slug, String description, String logoKey) {
        this.name = Objects.requireNonNull(name, "name không được để trống");
        this.slug = Objects.requireNonNull(slug, "slug không được để trống");
        this.description = description;
        this.logoKey = logoKey;
        this.status = BrandStatus.ACTIVE;
    }

    public static Brand create(String name, String slug, String description, String logoKey) {
        return new Brand(name, slug, description, logoKey);
    }

    public void update(String name, String slug, String description, String logoKey) {
        this.name = Objects.requireNonNull(name, "name không được để trống");
        this.slug = Objects.requireNonNull(slug, "slug không được để trống");
        this.description = description;
        this.logoKey = logoKey;
    }

    public void changeLogo(String logoKey) {
        this.logoKey = logoKey;
    }


    public void activate() {
        this.status = BrandStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = BrandStatus.INACTIVE;
    }
}
