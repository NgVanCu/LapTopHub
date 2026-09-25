package com.laptophub.cart.dto.response;

import com.laptophub.cart.entity.CartItem;
import com.laptophub.product.entity.ProductVariant;

public record CartLine(CartItem item, ProductVariant variant) {
}

