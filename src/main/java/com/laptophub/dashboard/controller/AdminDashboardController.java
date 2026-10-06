package com.laptophub.dashboard.controller;

import com.laptophub.dashboard.dto.response.*;
import com.laptophub.dashboard.service.DashboardService;
import com.laptophub.shared.response.ApiResponse;
import com.laptophub.shared.response.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 10;

    private final DashboardService dashboardService;

    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<RevenueSummaryResponse>> revenue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("Lấy báo cáo doanh thu thành công",dashboardService.getRevenue(from, to)));
    }

    @GetMapping("/orders-by-status")
    public ResponseEntity<ApiResponse<List<OrderStatusCountResponse>>> ordersByStatus(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("Lấy thống kê đơn hàng theo trạng thái thành công",dashboardService.getOrdersByStatus(from, to)));
    }

    @GetMapping("/customers")
    public ResponseEntity<ApiResponse<CustomerStatsResponse>> customers(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("Lấy thống kê khách hàng thành công",dashboardService.getCustomerStats(from, to)));
    }

    @GetMapping("/best-selling-products")
    public ResponseEntity<ApiResponse<List<BestSellingItemResponse>>> bestSellingProducts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success( "Lấy danh sách sản phẩm bán chạy thành công",dashboardService.getBestSellingProducts(from, to, limit)));
    }

    // page/size tự dựng PageRequest (không nhận Pageable trực tiếp) — sort cố
    // định theo available tăng dần (xem InventoryBalanceRepository.findLowStock),
    // giống lý do ở AdminOrderController/AdminStockReceiptController.
    @GetMapping("/low-stock-products")
    public ResponseEntity<ApiResponse<PageResponse<LowStockItemResponse>>> lowStockProducts(
            @RequestParam(defaultValue = "" + DEFAULT_LOW_STOCK_THRESHOLD) int threshold,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE));
        var result = dashboardService.getLowStockProducts(threshold, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách sản phẩm sắp hết hàng thành công",PageResponse.of(result)));
    }
}
