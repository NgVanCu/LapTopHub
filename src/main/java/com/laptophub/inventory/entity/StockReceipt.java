package com.laptophub.inventory.entity;

import com.laptophub.inventory.enums.InventoryMovementType;
import com.laptophub.inventory.enums.StockReceiptStatus;
import com.laptophub.shared.entity.BaseEntity;
import com.laptophub.shared.exception.AppException;
import com.laptophub.shared.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "stock_receipts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockReceipt extends BaseEntity {

    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StockReceiptStatus status;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    @Column(name = "confirmed_by_user_id")
    private Long confirmedByUserId;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "cancelled_by_user_id")
    private Long cancelledByUserId;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    private StockReceipt(String code, String note, Long createdByUserId) {
        this.code = Objects.requireNonNull(code, "code không được để trống");
        this.note = note;
        this.createdByUserId = Objects.requireNonNull(createdByUserId, "createdByUserId không được để trống");
        this.status = StockReceiptStatus.DRAFT;
    }

    public static StockReceipt create(String code, String note, Long createdByUserId) {
        return new StockReceipt(code, note, createdByUserId);
    }

    public void confirm(Long confirmedByUserId, Instant confirmedAt) {
        if (this.status != StockReceiptStatus.DRAFT) {
            throw new AppException(ErrorCode.INVALID_STOCK_RECEIPT_STATUS);
        }
        this.status = StockReceiptStatus.CONFIRMED;
        this.confirmedByUserId = Objects.requireNonNull(confirmedByUserId, "confirmedByUserId không được để trống");
        this.confirmedAt = Objects.requireNonNull(confirmedAt, "confirmedAt không được để trống");
    }

    public void cancel(Long cancelledByUserId, Instant cancelledAt) {
        if (this.status != StockReceiptStatus.DRAFT) {
            throw new AppException(ErrorCode.INVALID_STOCK_RECEIPT_STATUS);
        }
        this.status = StockReceiptStatus.CANCELLED;
        this.cancelledByUserId = Objects.requireNonNull(cancelledByUserId, "cancelledByUserId không được để trống");
        this.cancelledAt = Objects.requireNonNull(cancelledAt, "cancelledAt không được để trống");
    }
}
