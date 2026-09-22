package com.laptophub.product.dto.response;

import java.util.List;

public record ComparisonResult(
        List<ComparisonItem> items,
        List<ComparisonSpecRow> specifications) {
}
