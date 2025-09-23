package com.startup.ecommerce.v1.services.impl;

import com.startup.ecommerce.v1.entities.CartEntity;
import com.startup.ecommerce.v1.entities.CartItemEntity;
import com.startup.ecommerce.v1.entities.ProductEntity;
import com.startup.ecommerce.v1.entities.ProductVariantEntity;
import com.startup.ecommerce.v1.repositories.CartItemRepository;
import com.startup.ecommerce.v1.repositories.CartRepository;
import com.startup.ecommerce.v1.repositories.ProductRepository;
import com.startup.ecommerce.v1.repositories.ProductVariantRepository;
import com.startup.ecommerce.v1.services.CartService;
import com.startup.ecommerce.v1.services.StockService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.startup.ecommerce.v1.entities.UserEntity;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final StockService stockService;

    @Override
    @Transactional
    public CartEntity getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            CartEntity cart = CartEntity.builder()
                    .user(UserEntity.builder().id(userId).build())
                    .build();
            return cartRepository.save(cart);
        });
    }

    @Override
    public CartEntity getCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> CartEntity.builder()
                .user(UserEntity.builder().id(userId).build())
                .build());
    }

    @Override
    @Transactional
    public CartEntity addItem(Long userId, Long productId, Long variantId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        CartEntity cart = getOrCreateCart(userId);
        ProductEntity product = productRepository.findById(productId).orElseThrow();
        ProductVariantEntity variant = null;
        if (variantId != null) {
            variant = productVariantRepository.findById(variantId).orElseThrow();
            if (!variant.getProduct().getId().equals(product.getId())) {
                throw new IllegalArgumentException("La variante no pertenece al producto indicado");
            }
        }

        // Reservar stock a nivel de producto
        stockService.reserveStock(productId, quantity);

        double unitPrice = product.getPrice();
        CartItemEntity item = CartItemEntity.builder()
                .cart(cart)
                .product(product)
                .variant(variant)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .totalPrice(unitPrice * quantity)
                .build();
        cart.addItem(item);
        recalculateTotals(cart);
        cartItemRepository.save(item);
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public CartEntity updateItem(Long userId, Long cartItemId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        CartEntity cart = getOrCreateCart(userId);
        CartItemEntity item = cartItemRepository.findById(cartItemId).orElseThrow();
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("El item no pertenece al carrito del usuario");
        }

        int delta = quantity - item.getQuantity();
        if (delta > 0) {
            stockService.reserveStock(item.getProduct().getId(), delta);
        } else if (delta < 0) {
            stockService.releaseStock(item.getProduct().getId(), -delta);
        }

        item.setQuantity(quantity);
        item.setTotalPrice(item.getUnitPrice() * quantity);
        cartItemRepository.save(item);
        recalculateTotals(cart);
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void removeItem(Long userId, Long cartItemId) {
        CartEntity cart = getOrCreateCart(userId);
        CartItemEntity item = cartItemRepository.findById(cartItemId).orElseThrow();
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("El item no pertenece al carrito del usuario");
        }
        stockService.releaseStock(item.getProduct().getId(), item.getQuantity());
        cart.removeItem(item);
        cartItemRepository.delete(item);
        recalculateTotals(cart);
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        CartEntity cart = getOrCreateCart(userId);
        if (cart.getItems() != null) {
            for (CartItemEntity item : cart.getItems()) { // Bucle que recorre todos los items del carrito
                stockService.releaseStock(item.getProduct().getId(), item.getQuantity()); // Liberar el stock del producto || Cantidad a liberar 
            }
            cart.getItems().clear(); // Elimina todos los items de la lista del carrito
        }
        cart.setTotalPrice(0.0);
        cartRepository.save(cart);
    }

    @Override
    public CartEntity applyDiscount(Long userId, String code) {
        // Placeholder: lógica de descuentos se implementará con entidad Discount
        CartEntity cart = getOrCreateCart(userId);
        return cart;
    }

    private void recalculateTotals(CartEntity cart) {
        double total = 0.0;
        if (cart.getItems() != null) {
            for (CartItemEntity item : cart.getItems()) {
                total += item.getTotalPrice();
            }
        }
        cart.setTotalPrice(total);
    }
}


