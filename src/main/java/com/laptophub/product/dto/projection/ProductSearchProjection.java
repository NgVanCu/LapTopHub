package com.laptophub.product.dto.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ProductSearchProjection {

    Long getId();

    String getName();

    String getSlug();

    Long getCategoryId();

    Long getBrandId();

    BigDecimal getPriceFrom();

    BigDecimal getPriceTo();

    LocalDateTime getCreatedAt();
}