package com.laptophub.order.dto.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

// Projection cho native query GROUP BY DATE(created_at) — mỗi ngày luôn có
// ít nhất 1 đơn (do GROUP BY chỉ trả hàng có dữ liệu) nên revenue/orderCount
// không bao giờ null.
public interface RevenueByDayProjection {

    LocalDate getDay();

    BigDecimal getRevenue();

    long getOrderCount();
}
