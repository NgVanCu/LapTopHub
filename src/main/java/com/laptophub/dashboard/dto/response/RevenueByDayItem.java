package com.laptophub.dashboard.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RevenueByDayItem(LocalDate date, BigDecimal revenue, long orderCount) {
}
