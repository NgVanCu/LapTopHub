package com.laptophub.inventory.service;

import com.laptophub.inventory.entity.InventoryBalance;
import com.laptophub.inventory.entity.InventoryMovement;
import com.laptophub.inventory.enums.InventoryMovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryService {
    InventoryBalance getBalance(Long productVariantId);

    Page<InventoryMovement> listMovements(Long productVariantId, InventoryMovementType type, Pageable pageable);

    Page<InventoryBalance> listLowStock(int threshold, Pageable pageable);

    InventoryBalance receiveStock(Long productVariantId, int quantity, String referenceType,
                                  Long referenceId, Long actingUserId);

    InventoryBalance receiveReturn(Long productVariantId, int quantity, String referenceType,
                                   Long referenceId);

    InventoryBalance adjust(Long productVariantId, int delta, String reason, Long actingUserId);

    InventoryBalance reserve(Long productVariantId, int quantity, String referenceType, Long referenceId);

    InventoryBalance release(Long productVariantId, int quantity, String referenceType, Long referenceId);

    InventoryBalance fulfill(Long productVariantId, int quantity, String referenceType, Long referenceId);
}
