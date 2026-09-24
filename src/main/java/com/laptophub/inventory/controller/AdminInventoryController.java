package com.laptophub.inventory.controller;

import com.laptophub.inventory.dto.request.InventoryAdjustRequest;
import com.laptophub.inventory.dto.response.InventoryBalanceResponse;
import com.laptophub.inventory.dto.response.InventoryMovementResponse;
import com.laptophub.inventory.entity.InventoryBalance;
import com.laptophub.inventory.enums.InventoryMovementType;
import com.laptophub.inventory.service.InventoryService;
import com.laptophub.product.service.ProductVariantService;
import com.laptophub.security.currentuser.CurrentUserProvider;
import com.laptophub.shared.response.ApiResponse;
import com.laptophub.shared.response.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/inventory")
public class AdminInventoryController {
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final InventoryService inventoryService;
    private final ProductVariantService productVariantService;
    private final CurrentUserProvider currentUserProvider;

    public AdminInventoryController(InventoryService inventoryService, ProductVariantService productVariantService,
                                    CurrentUserProvider currentUserProvider) {
        this.inventoryService = inventoryService;
        this.productVariantService = productVariantService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/{variantId}/balance")
    public ResponseEntity<ApiResponse<InventoryBalanceResponse>> getBalance(@PathVariable Long variantId) {
        InventoryBalance balance = inventoryService.getBalance(variantId);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin tồn kho thành công",toResponse(balance)));
    }

    @GetMapping("/{variantId}/movements")
    public ResponseEntity<ApiResponse<PageResponse<InventoryMovementResponse>>> listMovements(
            @PathVariable Long variantId, @RequestParam(required = false) InventoryMovementType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE));
        Page<InventoryMovementResponse> result = inventoryService.listMovements(variantId, type, pageable)
                .map(InventoryMovementResponse::from);
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử biến động tồn kho thành công",PageResponse.of(result)));
    }

    @PostMapping("/{variantId}/adjustments")
    public ResponseEntity<ApiResponse<InventoryBalanceResponse>> adjust(@PathVariable Long variantId,
                                                                        @Valid @RequestBody InventoryAdjustRequest request) {
        Long actingAdminId = currentUserProvider.getCurrentUser().userId();
        InventoryBalance balance = inventoryService.adjust(variantId, request.delta(), request.reason(),
                actingAdminId);
        return ResponseEntity.ok(ApiResponse.success( "Điều chỉnh tồn kho thành công",toResponse(balance)));
    }

    private InventoryBalanceResponse toResponse(InventoryBalance balance) {
        String sku = productVariantService.getByIdOrThrow(balance.getProductVariantId()).getSku();
        return InventoryBalanceResponse.from(balance, sku);
    }

}
