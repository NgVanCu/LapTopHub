package com.laptophub.order.dto.response;


import com.laptophub.order.entity.ReturnRequest;
import com.laptophub.order.enums.ReturnRequestStatus;

import java.time.Instant;

public record ReturnRequestResponse(
        Long id,
        Long orderId,
        Long userId,
        String reason,
        ReturnRequestStatus status,
        Long decidedByUserId,
        Instant decidedAt,
        String decisionNote,
        Instant createdAt,
        Instant updatedAt) {

    public static ReturnRequestResponse from(ReturnRequest request) {
        return new ReturnRequestResponse(
                request.getId(),
                request.getOrderId(),
                request.getUserId(),
                request.getReason(),
                request.getStatus(),
                request.getDecidedByUserId(),
                request.getDecidedAt(),
                request.getDecisionNote(),
                request.getCreatedAt(),
                request.getUpdatedAt());
    }
}