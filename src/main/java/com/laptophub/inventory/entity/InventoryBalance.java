package com.laptophub.inventory.entity;

import com.laptophub.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Table(name = "inventory_balances")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryBalance extends BaseEntity {

    @Column(name = "product_variant_id", nullable = false, unique = true)
    private Long productVariantId;

    @Column(name = "on_hand_quantity", nullable = false)
    private Integer onHandQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity;

    private InventoryBalance(Long productVariantId) {
        this.productVariantId = Objects.requireNonNull(productVariantId, "productVariantId không được để trống");
        this.onHandQuantity = 0;
        this.reservedQuantity = 0;
    }

    public static InventoryBalance create(Long productVariantId) {
        return new InventoryBalance(productVariantId);
    }

    public int getAvailableQuantity() {
        return onHandQuantity - reservedQuantity;
    }
}
