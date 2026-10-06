package com.laptophub.order.service;

import com.laptophub.order.entity.ReturnRequest;
import com.laptophub.order.enums.ReturnRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReturnRequestService {
    ReturnRequest create(Long userId, Long orderId, String reason);

    ReturnRequest approve(Long requestId, Long actingAdminId);

    ReturnRequest reject(Long requestId, Long actingAdminId, String decisionNote);

    ReturnRequest getByIdOrThrow(Long requestId);

    ReturnRequest getOwnedOrThrow(Long userId, Long requestId);

    Page<ReturnRequest> listAdmin(ReturnRequestStatus status, Pageable pageable);
}
