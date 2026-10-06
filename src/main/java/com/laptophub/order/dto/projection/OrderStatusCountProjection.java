package com.laptophub.order.dto.projection;

import com.laptophub.order.enums.OrderStatus;

public interface OrderStatusCountProjection {

    OrderStatus getStatus();

    long getCount();
}

