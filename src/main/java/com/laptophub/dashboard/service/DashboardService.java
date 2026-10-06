package com.laptophub.dashboard.service;

import com.laptophub.dashboard.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface DashboardService {
    RevenueSummaryResponse getRevenue(LocalDate from, LocalDate to);

    List<OrderStatusCountResponse> getOrdersByStatus(LocalDate from, LocalDate to);

    CustomerStatsResponse getCustomerStats(LocalDate from, LocalDate to);

    List<BestSellingItemResponse> getBestSellingProducts(LocalDate from, LocalDate to, int limit);

    Page<LowStockItemResponse> getLowStockProducts(int threshold, Pageable pageable);
}
