package com.laptophub.inventory.controller;

import com.laptophub.inventory.dto.request.StockReceiptCreateRequest;
import com.laptophub.inventory.dto.request.StockReceiptItemsReplaceRequest;
import com.laptophub.inventory.dto.response.StockReceiptDetailResponse;
import com.laptophub.inventory.dto.response.StockReceiptItemResponse;
import com.laptophub.inventory.dto.response.StockReceiptSummaryResponse;
import com.laptophub.inventory.entity.StockReceipt;
import com.laptophub.inventory.enums.StockReceiptStatus;
import com.laptophub.inventory.service.StockReceiptService;
import com.laptophub.product.service.ProductVariantService;
import com.laptophub.security.currentuser.CurrentUserProvider;
import com.laptophub.shared.response.ApiResponse;
import com.laptophub.shared.response.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/stock-receipts")
public class AdminStockReceiptController {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final StockReceiptService stockReceiptService;
    private final ProductVariantService productVariantService;
    private final CurrentUserProvider currentUserProvider;

    public AdminStockReceiptController(StockReceiptService stockReceiptService,
                                       ProductVariantService productVariantService, CurrentUserProvider currentUserProvider) {
        this.stockReceiptService = stockReceiptService;
        this.productVariantService = productVariantService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StockReceiptDetailResponse>> create(
            @Valid @RequestBody StockReceiptCreateRequest request) {
        Long actingAdminId = currentUserProvider.getCurrentUser().userId();
        StockReceipt receipt = stockReceiptService.create(request, actingAdminId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tạo phiếu nhập kho thành công",toDetailResponse(receipt)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<StockReceiptSummaryResponse>>> list(
            @RequestParam(required = false) StockReceiptStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE));
        Page<StockReceiptSummaryResponse> result = stockReceiptService.list(status, pageable)
                .map(StockReceiptSummaryResponse::from);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách phiếu nhập kho thành công",PageResponse.of(result)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StockReceiptDetailResponse>> getOne(@PathVariable Long id) {
        StockReceipt receipt = stockReceiptService.getByIdOrThrow(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin phiếu nhập kho thành công",toDetailResponse(receipt)));
    }

    @PutMapping("/{id}/items")
    public ResponseEntity<ApiResponse<StockReceiptDetailResponse>> replaceItems(@PathVariable Long id,
                                                                                @Valid @RequestBody StockReceiptItemsReplaceRequest request) {
        StockReceipt receipt = stockReceiptService.replaceItems(id, request.items());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật dòng hàng của phiếu nhập kho thành công",toDetailResponse(receipt)));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<StockReceiptDetailResponse>> confirm(@PathVariable Long id) {
        Long actingAdminId = currentUserProvider.getCurrentUser().userId();
        StockReceipt receipt = stockReceiptService.confirm(id, actingAdminId);
        return ResponseEntity.ok(ApiResponse.success("Xác nhận phiếu nhập kho thành công",toDetailResponse(receipt)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<StockReceiptDetailResponse>> cancel(@PathVariable Long id) {
        Long actingAdminId = currentUserProvider.getCurrentUser().userId();
        StockReceipt receipt = stockReceiptService.cancel(id, actingAdminId);
        return ResponseEntity.ok(ApiResponse.success("Hủy phiếu nhập kho thành công",toDetailResponse(receipt)));
    }

    private StockReceiptDetailResponse toDetailResponse(StockReceipt receipt) {
        var items = stockReceiptService.getItems(receipt.getId()).stream()
                .map(item -> StockReceiptItemResponse.from(item,
                        productVariantService.getByIdOrThrow(item.getProductVariantId()).getSku()))
                .toList();
        return StockReceiptDetailResponse.from(receipt, items);
    }
}
