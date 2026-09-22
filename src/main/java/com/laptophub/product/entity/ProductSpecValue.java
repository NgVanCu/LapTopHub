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
@Table(name = "product_spec_values")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
//lưu giá trị thực tế của từng thông số đối với một sản phẩm cụ thể
public class ProductSpecValue extends BaseEntity {
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "specification_id", nullable = false)
    private Long specificationDefinitionId;

    @Column(name = "value", nullable = false, length = 500)
    private String value;

    private ProductSpecValue(Long productId, Long specificationDefinitionId, String value) {
        this.productId = Objects.requireNonNull(productId, "productId không được để trống");
        this.specificationDefinitionId =
                Objects.requireNonNull(specificationDefinitionId, "specificationDefinitionId không được để trống");
        this.value = Objects.requireNonNull(value, "value không được để trống");
    }

    public static ProductSpecValue create(Long productId, Long specificationDefinitionId, String value) {
        return new ProductSpecValue(productId, specificationDefinitionId, value);
    }

    public void changeValue(String value) {
        this.value = Objects.requireNonNull(value, "value không được để trống");
    }

}
