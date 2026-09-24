package com.laptophub.inventory.service;

import com.laptophub.inventory.dto.request.StockReceiptCreateRequest;
import com.laptophub.inventory.dto.request.StockReceiptItemRequest;
import com.laptophub.inventory.entity.StockReceipt;
import com.laptophub.inventory.entity.StockReceiptItem;
import com.laptophub.inventory.enums.StockReceiptStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StockReceiptService {
    StockReceipt create(StockReceiptCreateRequest request, Long createdByUserId);

    StockReceipt replaceItems(Long receiptId, List<StockReceiptItemRequest> items);

    StockReceipt confirm(Long receiptId, Long confirmedByUserId);

    StockReceipt cancel(Long receiptId, Long cancelledByUserId);

    StockReceipt getByIdOrThrow(Long receiptId);

    List<StockReceiptItem> getItems(Long receiptId);

    Page<StockReceipt> list(StockReceiptStatus status, Pageable pageable);


}
