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
@Table(name = "stock_receipt_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
//chi tiết phiếu hàng
public class StockReceiptItem extends BaseEntity {

    @Column(name = "stock_receipt_id", nullable = false)
    private Long stockReceiptId;

    @Column(name = "product_variant_id", nullable = false)
    private Long productVariantId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    private StockReceiptItem(Long stockReceiptId, Long productVariantId, Integer quantity) {
        this.stockReceiptId = Objects.requireNonNull(stockReceiptId, "stockReceiptId không được để trống");
        this.productVariantId = Objects.requireNonNull(productVariantId, "productVariantId không được để trống");
        this.quantity = Objects.requireNonNull(quantity, "quantity không được để trống");
    }

    public static StockReceiptItem create(Long stockReceiptId, Long productVariantId, Integer quantity) {
        return new StockReceiptItem(stockReceiptId, productVariantId, quantity);
    }
}
