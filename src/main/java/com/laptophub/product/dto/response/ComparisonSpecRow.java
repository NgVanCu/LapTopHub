package com.laptophub.product.dto.response;

import java.util.Map;

public record ComparisonSpecRow(
        String code,
        String label,
        String unit,
        String groupLabel,
        Map<Long, String> values) {
}
