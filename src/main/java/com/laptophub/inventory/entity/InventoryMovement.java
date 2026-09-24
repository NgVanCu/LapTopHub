package com.laptophub.inventory.entity;

import com.laptophub.inventory.enums.InventoryMovementType;
import com.laptophub.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Table(name = "inventory_movements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryMovement extends BaseEntity {

    @Column(name = "product_variant_id", nullable = false)
    private Long productVariantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private InventoryMovementType type;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "on_hand_after", nullable = false)
    private Integer onHandAfter;

    @Column(name = "reserved_after", nullable = false)
    private Integer reservedAfter;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    private InventoryMovement(Long productVariantId, InventoryMovementType type, Integer quantity,
                              Integer onHandAfter, Integer reservedAfter, String referenceType, Long referenceId, String reason,
                              Long createdByUserId) {
        this.productVariantId = Objects.requireNonNull(productVariantId, "productVariantId không được để trống");
        this.type = Objects.requireNonNull(type, "type không được để trống");
        this.quantity = Objects.requireNonNull(quantity, "quantity không được để trống");
        this.onHandAfter = Objects.requireNonNull(onHandAfter, "onHandAfter không được để trống");
        this.reservedAfter = Objects.requireNonNull(reservedAfter, "reservedAfter không được để trống");
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.reason = reason;
        this.createdByUserId = createdByUserId;
    }

    public static InventoryMovement create(Long productVariantId, InventoryMovementType type, Integer quantity,
                                           Integer onHandAfter, Integer reservedAfter, String referenceType, Long referenceId, String reason,
                                           Long createdByUserId) {
        return new InventoryMovement(productVariantId, type, quantity, onHandAfter, reservedAfter, referenceType,
                referenceId, reason, createdByUserId);
    }
}
