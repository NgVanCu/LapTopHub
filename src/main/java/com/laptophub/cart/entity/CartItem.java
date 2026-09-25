package com.laptophub.cart.entity;

import com.laptophub.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Table(name = "cart_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem extends BaseEntity {

    @Column(name = "cart_id", nullable = false)
    private Long cartId;

    @Column(name = "product_variant_id", nullable = false)
    private Long productVariantId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    private CartItem(Long cartId, Long productVariantId, int quantity) {
        this.cartId = Objects.requireNonNull(cartId, "cartId must not be null");
        this.productVariantId = Objects.requireNonNull(productVariantId, "productVariantId must not be null");
        requirePositive(quantity);
        this.quantity = quantity;
    }

    public static CartItem create(Long cartId, Long productVariantId, int quantity) {
        return new CartItem(cartId, productVariantId, quantity);
    }

    public void increaseQuantity(int delta) {
        requirePositive(delta);
        this.quantity += delta;
    }

    public void changeQuantity(int quantity) {
        requirePositive(quantity);
        this.quantity = quantity;
    }

    private static void requirePositive(int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
    }
}
