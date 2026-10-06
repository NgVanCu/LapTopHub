package com.laptophub.order.entity;

import com.laptophub.shared.entity.BaseEntity;
import com.laptophub.order.enums.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Table(name = "order_status_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderStatusHistory extends BaseEntity {

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", nullable = false, length = 20)
    private OrderStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 20)
    private OrderStatus toStatus;

    // Nullable từ V28: NULL nghĩa là hệ thống tự động thực hiện transition
    // (vd PaymentExpirySweepScheduler tự hủy đơn quá hạn thanh toán), không
    // phải 1 user thật thao tác.
    @Column(name = "changed_by_user_id")
    private Long changedByUserId;

    @Column(name = "note", length = 500)
    private String note;

    private OrderStatusHistory(Long orderId, OrderStatus fromStatus, OrderStatus toStatus, Long changedByUserId,
                               String note) {
        this.orderId = Objects.requireNonNull(orderId, "orderId không được để trống");
        this.fromStatus = Objects.requireNonNull(fromStatus, "fromStatus không được để trống");
        this.toStatus = Objects.requireNonNull(toStatus, "toStatus không được để trống");
        this.changedByUserId = changedByUserId;
        this.note = note;
    }

    public static OrderStatusHistory create(Long orderId, OrderStatus fromStatus, OrderStatus toStatus,
                                            Long changedByUserId, String note) {
        return new OrderStatusHistory(orderId, fromStatus, toStatus, changedByUserId, note);
    }
}
