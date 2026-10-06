package com.laptophub.order.dto.response;

import com.laptophub.order.entity.Order;
import com.laptophub.order.enums.OrderStatus;
import com.laptophub.order.enums.PaymentMethod;
import com.laptophub.payment.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
// Dùng cho GET /customer/orders (danh sách) — không kèm items để tránh N+1
// khi phân trang; xem chi tiết từng đơn qua GET /customer/orders/{id}
// (OrderResponse). paymentStatus null nghĩa là đơn COD (không có Payment).

public record OrderSummaryResponse(
        Long id,
        OrderStatus status,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        BigDecimal totalAmount,
        Instant createdAt) {

    public static OrderSummaryResponse from(Order order, PaymentStatus paymentStatus) {
        return new OrderSummaryResponse(
                order.getId(),
                order.getStatus(),
                order.getPaymentMethod(),
                paymentStatus,
                order.getTotalAmount(),
                order.getCreatedAt());
    }
}
