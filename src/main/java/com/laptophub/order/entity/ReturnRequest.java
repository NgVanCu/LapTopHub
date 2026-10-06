package com.laptophub.order.entity;

import com.laptophub.order.enums.ReturnRequestStatus;
import com.laptophub.shared.entity.BaseEntity;
import com.laptophub.shared.exception.AppException;
import com.laptophub.shared.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;

// Yêu cầu trả hàng ở mức CẢ ĐƠN (whole-order), không phải từng dòng hàng —
// đúng tinh thần MVP của MODULE_PLAN.md, không cần bảng return_request_items.
// Chỉ 2 transition (approve/reject) nên dùng audit-column-per-transition
// (decidedByUserId/decidedAt/decisionNote) giống StockReceipt, không cần
// bảng lịch sử riêng như OrderStatusHistory (chỉ hợp lý khi có nhiều
// transition — xem Order).

@Entity
@Table(name = "return_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReturnRequest extends BaseEntity {

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    // Denormalized từ orders.user_id — tránh phải join orders khi kiểm tra
    // ownership (giống Address.userId, OrderItem không cần vì Order đã có sẵn).
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReturnRequestStatus status;

    @Column(name = "decided_by_user_id")
    private Long decidedByUserId;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "decision_note", length = 500)
    private String decisionNote;

    private ReturnRequest(Long orderId, Long userId, String reason) {
        this.orderId = Objects.requireNonNull(orderId, "orderId không được để trống");
        this.userId = Objects.requireNonNull(userId, "userId không được để trống");
        this.reason = Objects.requireNonNull(reason, "reason không được để trống");
        this.status = ReturnRequestStatus.REQUESTED;
    }

    public static ReturnRequest create(Long orderId, Long userId, String reason) {
        return new ReturnRequest(orderId, userId, reason);
    }

    public void approve(Long decidedByUserId, Instant decidedAt) {
        requireRequested();
        this.status = ReturnRequestStatus.APPROVED;
        this.decidedByUserId = Objects.requireNonNull(decidedByUserId, "decidedByUserId không được để trống");
        this.decidedAt = Objects.requireNonNull(decidedAt, "decidedAt không được để trống");
    }

    public void reject(Long decidedByUserId, Instant decidedAt, String decisionNote) {
        requireRequested();
        this.status = ReturnRequestStatus.REJECTED;
        this.decidedByUserId = Objects.requireNonNull(decidedByUserId, "decidedByUserId không được để trống");
        this.decidedAt = Objects.requireNonNull(decidedAt, "decidedAt không được để trống");
        this.decisionNote = decisionNote;
    }

    private void requireRequested() {
        if (this.status != ReturnRequestStatus.REQUESTED) {
            throw new AppException(ErrorCode.INVALID_RETURN_REQUEST_STATUS);
        }
    }
}
