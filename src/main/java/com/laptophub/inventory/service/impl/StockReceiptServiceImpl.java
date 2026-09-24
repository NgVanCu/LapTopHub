package com.laptophub.inventory.service.impl;

import com.laptophub.inventory.dto.request.StockReceiptCreateRequest;
import com.laptophub.inventory.dto.request.StockReceiptItemRequest;
import com.laptophub.inventory.entity.StockReceipt;
import com.laptophub.inventory.entity.StockReceiptItem;
import com.laptophub.inventory.enums.StockReceiptStatus;
import com.laptophub.inventory.repository.StockReceiptItemRepository;
import com.laptophub.inventory.repository.StockReceiptRepository;
import com.laptophub.inventory.service.InventoryService;
import com.laptophub.inventory.service.StockReceiptService;
import com.laptophub.product.service.ProductVariantService;
import com.laptophub.shared.exception.AppException;
import com.laptophub.shared.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StockReceiptServiceImpl implements StockReceiptService {

    private final StockReceiptRepository stockReceiptRepository;
    private final StockReceiptItemRepository stockReceiptItemRepository;
    private final ProductVariantService productVariantService;
    private final InventoryService inventoryService;

    public StockReceiptServiceImpl(StockReceiptRepository stockReceiptRepository,
                                   StockReceiptItemRepository stockReceiptItemRepository,
                                   ProductVariantService productVariantService,
                                   InventoryService inventoryService) {
        this.stockReceiptRepository = stockReceiptRepository;
        this.stockReceiptItemRepository = stockReceiptItemRepository;
        this.productVariantService = productVariantService;
        this.inventoryService = inventoryService;
    }
    @Override
    @Transactional
    public StockReceipt create(StockReceiptCreateRequest request, Long createdByUserId) {
        if (stockReceiptRepository.existsByCode(request.code())) {
            throw new AppException(ErrorCode.RESOURCE_CONFLICT, "Mã phiếu nhập đã tồn tại");
        }
        validateItems(request.items());

        StockReceipt receipt = stockReceiptRepository
                .save(StockReceipt.create(request.code(), request.note(), createdByUserId));
        saveItems(receipt.getId(), request.items());
        return receipt;
    }

    @Override
    @Transactional
    public StockReceipt replaceItems(Long receiptId, List<StockReceiptItemRequest> items) {
        StockReceipt receipt = getByIdOrThrow(receiptId);
        if (receipt.getStatus() != StockReceiptStatus.DRAFT) {
            throw new AppException(ErrorCode.INVALID_STOCK_RECEIPT_STATUS);
        }
        validateItems(items);

        stockReceiptItemRepository.deleteByStockReceiptId(receiptId);
        saveItems(receiptId, items);
        return receipt;
    }

    // Gate atomic (StockReceiptRepository.confirmIfDraft) chạy TRƯỚC vòng lặp
    // gọi InventoryService — chỉ transaction thắng cuộc đua giành được quyền
    // chuyển DRAFT->CONFIRMED mới được cộng on_hand. Chống race 2 request
    // confirm() đồng thời cùng 1 phiếu (double stock-in). getByIdOrThrow đầu
    // tiên chỉ để trả đúng 404 khi id không tồn tại (gate atomic không phân
    // biệt được "không tồn tại" và "không còn DRAFT" — cả 2 đều trả 0 dòng).
    @Override
    @Transactional
    public StockReceipt confirm(Long receiptId, Long confirmedByUserId) {
        getByIdOrThrow(receiptId);

        int rows = stockReceiptRepository.confirmIfDraft(receiptId, confirmedByUserId, Instant.now());
        if (rows == 0) {
            throw new AppException(ErrorCode.INVALID_STOCK_RECEIPT_STATUS);
        }

        List<StockReceiptItem> items = stockReceiptItemRepository.findByStockReceiptId(receiptId);
        if (items.isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Phiếu nhập chưa có dòng hàng nào");
        }

        for (StockReceiptItem item : items) {
            inventoryService.receiveStock(item.getProductVariantId(), item.getQuantity(), "STOCK_RECEIPT", receiptId,
                    confirmedByUserId);
        }
        // InventoryBalanceRepository dùng @Modifying(clearAutomatically = true) —
        // xoá TOÀN BỘ persistence context, nên phải reload trước khi trả về.
        return getByIdOrThrow(receiptId);
    }

    @Override
    @Transactional
    public StockReceipt cancel(Long receiptId, Long cancelledByUserId) {
        StockReceipt receipt = getByIdOrThrow(receiptId);
        receipt.cancel(cancelledByUserId, Instant.now());
        return receipt;
    }
    @Override
    public StockReceipt getByIdOrThrow(Long receiptId) {
        return stockReceiptRepository.findById(receiptId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }
    @Override
    public List<StockReceiptItem> getItems(Long receiptId) {
        return stockReceiptItemRepository.findByStockReceiptId(receiptId);
    }
    @Override
    public Page<StockReceipt> list(StockReceiptStatus status, Pageable pageable) {
        return stockReceiptRepository.search(status, pageable);
    }

    private void validateItems(List<StockReceiptItemRequest> items) {
        long distinctVariantCount = items.stream().map(StockReceiptItemRequest::productVariantId).distinct().count();
        if (distinctVariantCount != items.size()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Phiếu nhập không được có 2 dòng cùng 1 biến thể");
        }
        items.forEach(item -> productVariantService.getByIdOrThrow(item.productVariantId()));
    }

    private void saveItems(Long receiptId, List<StockReceiptItemRequest> items) {
        List<StockReceiptItem> entities = items.stream()
                .map(item -> StockReceiptItem.create(receiptId, item.productVariantId(), item.quantity()))
                .collect(Collectors.toList());
        stockReceiptItemRepository.saveAll(entities);
    }
}
