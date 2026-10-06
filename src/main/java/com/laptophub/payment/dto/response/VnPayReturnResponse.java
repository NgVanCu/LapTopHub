package com.laptophub.payment.dto.response;

public record VnPayReturnResponse(boolean success, Long orderId, String message) {
}

