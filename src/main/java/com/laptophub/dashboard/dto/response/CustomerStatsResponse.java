package com.laptophub.dashboard.dto.response;

public record CustomerStatsResponse(
        long totalCustomers,
        long newCustomers,
        long activeCustomers,
        long blockedCustomers) {
}
