package com.laptophub.order.dto.response;


import com.laptophub.order.entity.Order;
import com.laptophub.order.enums.OrderStatus;
import com.laptophub.order.enums.PaymentMethod;
import com.laptophub.payment.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        OrderStatus status,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        BigDecimal totalAmount,
        BigDecimal discountAmount,
        String voucherCode,
        String note,
        String recipientName,
        String phone,
        String province,
        String ward,
        String streetAddress,
        List<OrderItemResponse> items,
        Instant createdAt,
        Instant updatedAt) {

    // items phải được caller build sẵn qua OrderService.toItemResponses (batch
    // resolve productId/productSlug theo productVariantId). paymentStatus lấy
    // qua OrderService.getPaymentStatus — null cho đơn COD (không có Payment).
    // Record này không tự enrich để tránh phụ thuộc service ngay trong DTO.
    public static OrderResponse from(Order order, List<OrderItemResponse> items, PaymentStatus paymentStatus) {
        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getPaymentMethod(),
                paymentStatus,
                order.getTotalAmount(),
                order.getDiscountAmount(),
                order.getVoucherCode(),
                order.getNote(),
                order.getRecipientName(),
                order.getPhone(),
                order.getProvince(),
                order.getWard(),
                order.getStreetAddress(),
                items,
                order.getCreatedAt(),
                order.getUpdatedAt());
    }
}
