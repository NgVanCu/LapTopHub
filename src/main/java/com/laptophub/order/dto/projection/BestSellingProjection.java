package com.laptophub.order.dto.projection;

import java.math.BigDecimal;

public interface BestSellingProjection {

    Long getProductVariantId();

    long getQuantitySold();

    BigDecimal getRevenue();
}
