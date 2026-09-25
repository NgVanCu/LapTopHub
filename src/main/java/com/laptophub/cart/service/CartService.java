package com.laptophub.cart.service;

import com.laptophub.cart.dto.response.CartLine;
import com.laptophub.cart.entity.CartItem;

import java.util.List;

public interface CartService {

    List<CartLine> getItemsWithLivePrice(Long userId);

    CartLine addItem(Long userId, Long productVariantId, int quantity);

    CartLine updateQuantity(Long userId, Long itemId, int quantity);

    void removeItem(Long userId, Long itemId);

    void clear(Long userId);

    List<CartItem> lockItemsForCheckout(Long userId);

}
