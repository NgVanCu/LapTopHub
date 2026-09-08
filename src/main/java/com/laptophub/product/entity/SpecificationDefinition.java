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
@Table(name = "specifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpecificationDefinition extends BaseEntity {
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "code", nullable = false, length = 100, unique = true)
    private String code;

    @Column(name = "label", nullable = false, length = 150)
    private String label;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "group_label", length = 100)
    private String groupLabel;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    private SpecificationDefinition(Long categoryId, String code, String label, String unit, String groupLabel,
                                    int displayOrder) {
        this.categoryId = categoryId;
        this.code = Objects.requireNonNull(code, "code không được để trống");
        this.label = Objects.requireNonNull(label, "label không được để trống");
        this.unit = unit;
        this.groupLabel = groupLabel;
        this.displayOrder = displayOrder;
    }

    public static SpecificationDefinition create(Long categoryId, String code, String label, String unit,
                                                 String groupLabel, int displayOrder) {
        return new SpecificationDefinition(categoryId, code, label, unit, groupLabel, displayOrder);
    }
}
