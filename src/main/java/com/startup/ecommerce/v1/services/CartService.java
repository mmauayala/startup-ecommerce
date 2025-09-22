package com.startup.ecommerce.v1.services;

import com.startup.ecommerce.v1.entities.CartEntity;

public interface CartService {

    CartEntity getOrCreateCart(Long userId);

    CartEntity getCart(Long userId);

    CartEntity addItem(Long userId, Long productId, Long variantId, Integer quantity);

    CartEntity updateItem(Long userId, Long cartItemId, Integer quantity);

    void removeItem(Long userId, Long cartItemId);

    void clearCart(Long userId);

    CartEntity applyDiscount(Long userId, String code);
}


