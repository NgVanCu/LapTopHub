package com.laptophub.dashboard.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record RevenueSummaryResponse(BigDecimal totalRevenue, long orderCount, List<RevenueByDayItem> byDay) {
}
