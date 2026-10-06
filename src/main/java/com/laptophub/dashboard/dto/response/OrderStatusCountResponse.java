package com.laptophub.dashboard.dto.response;

import com.laptophub.order.enums.OrderStatus;

public record OrderStatusCountResponse(OrderStatus status, long count) {
}
