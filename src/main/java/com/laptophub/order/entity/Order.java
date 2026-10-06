package com.laptophub.order.entity;

import com.laptophub.order.enums.OrderStatus;
import com.laptophub.order.enums.PaymentMethod;
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

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

// userId là FK dạng Long phẳng, đúng tiền lệ chung của dự án. recipientName..
// streetAddress là snapshot địa chỉ giao hàng tại thời điểm đặt đơn (không FK
// tới Address) — địa chỉ gốc có thể bị Customer sửa/xoá sau đó.
// paymentMethod nhận tham số từ Giai đoạn 7 (trước đó hardcode COD, chưa có
// luồng ONLINE). totalAmount là số tiền phải trả SAU khi trừ discountAmount
// (không phải tổng thô) — discountAmount/voucherId/voucherCode là snapshot
// voucher đã áp dụng lúc checkout (voucherId/voucherCode null nếu không dùng
// voucher), theo đúng triết lý snapshot đã dùng cho địa chỉ giao hàng.

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "voucher_id")
    private Long voucherId;

    @Column(name = "voucher_code", length = 50)
    private String voucherCode;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "recipient_name", nullable = false, length = 255)
    private String recipientName;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "province", nullable = false, length = 255)
    private String province;

    @Column(name = "ward", nullable = false, length = 255)
    private String ward;

    @Column(name = "street_address", nullable = false, length = 500)
    private String streetAddress;

    private Order(Long userId, PaymentMethod paymentMethod, BigDecimal totalAmount, BigDecimal discountAmount,
                  Long voucherId, String voucherCode, String note, String recipientName, String phone,
                  String province, String ward, String streetAddress) {
        this.userId = Objects.requireNonNull(userId, "userId không được để trống");
        this.status = OrderStatus.PENDING;
        this.paymentMethod = Objects.requireNonNull(paymentMethod, "paymentMethod không được để trống");
        this.totalAmount = Objects.requireNonNull(totalAmount, "totalAmount không được để trống");
        this.discountAmount = Objects.requireNonNull(discountAmount, "discountAmount không được để trống");
        this.voucherId = voucherId;
        this.voucherCode = voucherCode;
        this.note = note;
        this.recipientName = Objects.requireNonNull(recipientName, "recipientName không được để trống");
        this.phone = Objects.requireNonNull(phone, "phone không được để trống");
        this.province = Objects.requireNonNull(province, "province không được để trống");
        this.ward = Objects.requireNonNull(ward, "ward không được để trống");
        this.streetAddress = Objects.requireNonNull(streetAddress, "streetAddress không được để trống");
    }

    public static Order create(Long userId, PaymentMethod paymentMethod, BigDecimal totalAmount,
                               BigDecimal discountAmount, Long voucherId, String voucherCode, String note,
                               String recipientName, String phone, String province, String ward,
                               String streetAddress) {
        return new Order(userId,paymentMethod,totalAmount,discountAmount,voucherId,voucherCode,note,recipientName,phone,province,ward,streetAddress);
    }

    // Tập trạng thái được phép hủy là tập RỘNG NHẤT (năng lực Admin) —
    // OrderService.cancelByCustomer tự kiểm tra phạm vi hẹp hơn (chỉ
    // PENDING/CONFIRMED) TRƯỚC khi gọi cancel(), entity không phân biệt ai gọi.
    private static final Set<OrderStatus> CANCELLABLE_STATUSES =
            EnumSet.of(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.PREPARING);

    public void confirm() {
        requireStatus(OrderStatus.PENDING);
        this.status = OrderStatus.CONFIRMED;
    }

    public void prepare() {
        requireStatus(OrderStatus.CONFIRMED);
        this.status = OrderStatus.PREPARING;
    }

    public void ship() {
        requireStatus(OrderStatus.PREPARING);
        this.status = OrderStatus.SHIPPING;
    }

    public void deliver() {
        requireStatus(OrderStatus.SHIPPING);
        this.status = OrderStatus.DELIVERED;
    }

    public void cancel() {
        if (!isCancellable()) {
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS);
        }
        this.status = OrderStatus.CANCELLED;
    }

    // Dùng để OrderService fail-fast trước khi đụng inventory (đọc-only,
    // không mutate) — tránh gọi InventoryService.release rồi mới phát hiện
    // trạng thái không hợp lệ.
    public boolean isCancellable() {
        return CANCELLABLE_STATUSES.contains(this.status);
    }

    public void requestReturn() {
        requireStatus(OrderStatus.DELIVERED);
        this.status = OrderStatus.RETURN_REQUESTED;
    }

    public void approveReturn() {
        requireStatus(OrderStatus.RETURN_REQUESTED);
        this.status = OrderStatus.RETURNED;
    }

    public void rejectReturn() {
        requireStatus(OrderStatus.RETURN_REQUESTED);
        this.status = OrderStatus.DELIVERED;
    }

    private void requireStatus(OrderStatus expected) {
        if (this.status != expected) {
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS);
        }
    }
}
