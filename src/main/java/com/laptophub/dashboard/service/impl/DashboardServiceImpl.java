package com.laptophub.dashboard.service.impl;

import com.laptophub.dashboard.dto.response.*;
import com.laptophub.dashboard.service.DashboardService;
import com.laptophub.inventory.entity.InventoryBalance;
import com.laptophub.inventory.service.InventoryService;
import com.laptophub.order.dto.projection.BestSellingProjection;
import com.laptophub.order.dto.projection.RevenueByDayProjection;
import com.laptophub.order.service.OrderService;
import com.laptophub.product.entity.Product;
import com.laptophub.product.entity.ProductVariant;
import com.laptophub.product.service.ProductService;
import com.laptophub.product.service.ProductVariantService;
import com.laptophub.shared.exception.AppException;
import com.laptophub.shared.exception.ErrorCode;
import com.laptophub.user.enums.UserStatus;
import com.laptophub.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final int MAX_BEST_SELLING_LIMIT = 50;

    private final OrderService orderService;
    private final UserService userService;
    private final ProductVariantService productVariantService;
    private final ProductService productService;
    private final InventoryService inventoryService;

    public DashboardServiceImpl(OrderService orderService, UserService userService,
                            ProductVariantService productVariantService, ProductService productService,
                            InventoryService inventoryService) {
        this.orderService = orderService;
        this.userService = userService;
        this.productVariantService = productVariantService;
        this.productService = productService;
        this.inventoryService = inventoryService;
    }

    // from/to là LocalDate (thân thiện hơn Instant khi test Postman thủ
    // công) — quy đổi sang mốc UTC start-of-day; to là cận trên loại trừ
    // (dùng start-of-day của ngày KẾ TIẾP to) để trọn vẹn cả ngày to.
    @Override
    public RevenueSummaryResponse getRevenue(LocalDate from, LocalDate to) {
        InstantRange range = toInstantRange(from, to);
        List<RevenueByDayProjection> rows = orderService.getRevenueByDay(range.from(), range.toExclusive());

        BigDecimal totalRevenue = rows.stream().map(RevenueByDayProjection::getRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long orderCount = rows.stream().mapToLong(RevenueByDayProjection::getOrderCount).sum();
        List<RevenueByDayItem> byDay = rows.stream()
                .map(r -> new RevenueByDayItem(r.getDay(), r.getRevenue(), r.getOrderCount()))
                .toList();

        return new RevenueSummaryResponse(totalRevenue, orderCount, byDay);
    }

    // Không lọc status — muốn thấy toàn bộ vòng đời đơn hàng kể cả
    // PENDING/CANCELLED (khác getRevenue chỉ tính DELIVERED).
    @Override
    public List<OrderStatusCountResponse> getOrdersByStatus(LocalDate from, LocalDate to) {
        InstantRange range = toInstantRange(from, to);
        return orderService.countOrdersByStatus(range.from(), range.toExclusive()).stream()
                .map(row -> new OrderStatusCountResponse(row.getStatus(), row.getCount()))
                .toList();
    }

    // "newCustomers" cần cả 2 mốc from/to non-null (derived query
    // CreatedAtBetween) — bỏ trống thì quy về "toàn thời gian" (EPOCH ->
    // now, theo clock đã inject để test được deterministic).
    @Override
    public CustomerStatsResponse getCustomerStats(LocalDate from, LocalDate to) {
        InstantRange range = toInstantRange(from, to);
        Instant effectiveFrom = range.from() == null ? Instant.EPOCH : range.from();
        Instant effectiveTo = range.toExclusive() == null ? Instant.now(): range.toExclusive();

        long totalCustomers = userService.countCustomers();
        long newCustomers = userService.countNewCustomers(effectiveFrom, effectiveTo);
        long activeCustomers = userService.countCustomersByStatus(UserStatus.ACTIVE);
        long blockedCustomers = userService.countCustomersByStatus(UserStatus.BLOCKED);

        return new CustomerStatsResponse(totalCustomers, newCustomers, activeCustomers, blockedCustomers);
    }

    // Xếp hạng theo ProductVariant (SKU) — nhất quán với toàn bộ thiết kế hệ
    // thống (giỏ hàng/đơn hàng/tồn kho/giá đều key theo variant), tránh phải
    // aggregate 2 lần (DB rồi lại Java) nếu roll-up lên Product. limit được
    // kẹp về [1, MAX_BEST_SELLING_LIMIT] — đây là "top N" cho widget, không
    // phải phân trang đầy đủ.
    @Override
    public List<BestSellingItemResponse> getBestSellingProducts(LocalDate from, LocalDate to, int limit) {
        InstantRange range = toInstantRange(from, to);
        int safeLimit = Math.clamp(
                limit,
                1,
                MAX_BEST_SELLING_LIMIT
        );

        List<BestSellingProjection> rows = orderService.findBestSellingVariants(range.from(), range.toExclusive(),
                safeLimit);
        if (rows.isEmpty()) {
            return List.of();
        }

        List<Long> variantIds = rows.stream().map(BestSellingProjection::getProductVariantId).toList();
        Map<Long, ProductVariant> variantsById = productVariantService.findByIds(variantIds);
        List<Long> productIds = variantsById.values().stream().map(ProductVariant::getProductId).distinct().toList();
        Map<Long, Product> productsById = productService.findByIds(productIds);

        return rows.stream()
                .map(row -> {
                    ProductVariant variant = variantsById.get(row.getProductVariantId());
                    Product product = variant == null ? null : productsById.get(variant.getProductId());
                    return new BestSellingItemResponse(
                            row.getProductVariantId(), product == null ? null : product.getId(),
                            product == null ? null : product.getName(),
                            variant == null ? null : variant.getVariantName(),
                            variant == null ? null : variant.getSku(),
                            row.getQuantitySold(), row.getRevenue());
                })
                .toList();
    }

    // Không lọc theo ProductVariantStatus (xem
    // InventoryBalanceRepository.findLowStock) — giữ đúng phân trang ở tầng
    // DB. threshold âm không hợp lý (mọi available >= 0 rồi vẫn "không đạt
    // threshold âm" theo logic <=, nhưng chặn sớm cho rõ ràng thay vì trả
    // danh sách rỗng gây hiểu nhầm là lỗi).
    @Override
    public Page<LowStockItemResponse> getLowStockProducts(int threshold, Pageable pageable) {
        if (threshold < 0) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Ngưỡng tồn kho không được âm");
        }

        Page<InventoryBalance> page = inventoryService.listLowStock(threshold, pageable);
        if (page.isEmpty()) {
            return page.map(b -> null);
        }

        List<Long> variantIds = page.getContent().stream().map(InventoryBalance::getProductVariantId).toList();
        Map<Long, ProductVariant> variantsById = productVariantService.findByIds(variantIds);
        List<Long> productIds = variantsById.values().stream().map(ProductVariant::getProductId).distinct().toList();
        Map<Long, Product> productsById = productService.findByIds(productIds);

        return page.map(balance -> {
            ProductVariant variant = variantsById.get(balance.getProductVariantId());
            Product product = variant == null ? null : productsById.get(variant.getProductId());
            return new LowStockItemResponse(
                    balance.getProductVariantId(), product == null ? null : product.getId(),
                    product == null ? null : product.getName(),
                    variant == null ? null : variant.getVariantName(),
                    variant == null ? null : variant.getSku(),
                    balance.getOnHandQuantity(), balance.getReservedQuantity(), balance.getAvailableQuantity(),
                    variant == null ? null : variant.getStatus());
        });
    }

    private InstantRange toInstantRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Ngày bắt đầu phải trước hoặc bằng ngày kết thúc");
        }
        Instant fromInstant = from == null ? null : from.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant toInstantExclusive = to == null ? null : to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        return new InstantRange(fromInstant, toInstantExclusive);
    }

    private record InstantRange(Instant from, Instant toExclusive) {
    }
}

