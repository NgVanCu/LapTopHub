package com.laptophub.order.entity;

import com.laptophub.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "product_variant_id", nullable = false)
    private Long productVariantId;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "variant_name", length = 255)
    private String variantName;

    @Column(name = "sku", nullable = false, length = 100)
    private String sku;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;

    private OrderItem(Long orderId, Long productVariantId, String productName, String variantName, String sku,
                      BigDecimal unitPrice, int quantity) {
        this.orderId = Objects.requireNonNull(orderId, "orderId không được để trống");
        this.productVariantId = Objects.requireNonNull(productVariantId, "productVariantId không được để trống");
        this.productName = Objects.requireNonNull(productName, "productName không được để trống");
        this.variantName = variantName;
        this.sku = Objects.requireNonNull(sku, "sku không được để trống");
        this.unitPrice = Objects.requireNonNull(unitPrice, "unitPrice không được để trống");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity không được để trống");
        }
        this.quantity = quantity;
        this.discountAmount = BigDecimal.ZERO;
    }

    public static OrderItem create(Long orderId, Long productVariantId, String productName, String variantName,
                                   String sku, BigDecimal unitPrice, int quantity) {
        return new OrderItem(orderId, productVariantId, productName, variantName, sku, unitPrice, quantity);
    }
}
