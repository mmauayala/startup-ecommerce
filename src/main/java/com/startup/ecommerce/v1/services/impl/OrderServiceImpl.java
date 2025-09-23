package com.startup.ecommerce.v1.services.impl;

import com.startup.ecommerce.v1.dto.CreateOrderDto;
import com.startup.ecommerce.v1.dto.OrderDto;
import com.startup.ecommerce.v1.dto.OrderItemDto;
import com.startup.ecommerce.v1.entities.CartEntity;
import com.startup.ecommerce.v1.entities.CartItemEntity;
import com.startup.ecommerce.v1.entities.OrderEntity;
import com.startup.ecommerce.v1.entities.OrderItemEntity;
import com.startup.ecommerce.v1.entities.UserEntity;
import com.startup.ecommerce.v1.entities.enums.OrderStatus;
import com.startup.ecommerce.v1.exceptions.ResourceNotFoundException;
import com.startup.ecommerce.v1.repositories.CartRepository;
import com.startup.ecommerce.v1.repositories.OrderItemRepository;
import com.startup.ecommerce.v1.repositories.OrderRepository;
import com.startup.ecommerce.v1.repositories.UserRepository;
import com.startup.ecommerce.v1.services.OrderService;
import com.startup.ecommerce.v1.services.StockService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final StockService stockService;

    @Override
    @Transactional
    public OrderDto createOrder(Long userId, CreateOrderDto createOrderDto) {
        // Obtener el usuario y su carrito
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado"));

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("El carrito está vacío");
        }

        // Crear la orden
        OrderEntity order = new OrderEntity();
        order.setOrderNumber(generateOrderNumber());
        order.setUser(user);
        order.setShippingAddress(createOrderDto.getShippingAddress());
        order.setNotes(createOrderDto.getNotes());
        order.setStatus(OrderStatus.PENDIENTE);
        order.setTotalAmount(calculateTotal(cart.getItems()));

        // Guardar la orden
        OrderEntity savedOrder = orderRepository.save(order);

        // Crear y guardar los items de la orden
        Set<OrderItemEntity> orderItems = createOrderItems(cart.getItems(), savedOrder);
        savedOrder.setItems(orderItems);

        // Reservar el stock
        orderItems.forEach(item -> 
            stockService.reserveStock(item.getProduct().getId(), item.getQuantity())
        );

        // Limpiar el carrito
        cartRepository.delete(cart);

        return mapToOrderDto(savedOrder);
    }

    @Override
    public OrderDto getOrderById(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));
        return mapToOrderDto(order);
    }

    @Override
    public List<OrderDto> getOrdersByUserId(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        return orderRepository.findByUser(user, Pageable.unpaged())
                .getContent()
                .stream()
                .map(this::mapToOrderDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderDto updateOrderStatus(Long orderId, OrderStatus status) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));
        
        // Validar transiciones de estado
        validateStatusTransition(order.getStatus(), status);
        
        // Actualizar el estado
        order.setStatus(status);
        
        // Si la orden se completa, consumir el stock
        if (status == OrderStatus.ENTREGADO) {
            order.getItems().forEach(item -> 
                stockService.consumeStock(item.getProduct().getId(), item.getQuantity())
            );
        }
        
        // Si la orden se cancela, liberar el stock
        if (status == OrderStatus.CANCELADO) {
            order.getItems().forEach(item -> 
                stockService.releaseStock(item.getProduct().getId(), item.getQuantity())
            );
        }
        
        return mapToOrderDto(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderDto cancelOrder(Long orderId) {
        return updateOrderStatus(orderId, OrderStatus.CANCELADO);
    }

    // Métodos auxiliares privados

    private String generateOrderNumber() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private BigDecimal calculateTotal(Set<CartItemEntity> items) {
        return items.stream()
                .<BigDecimal>map(item -> item.getVariant().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Set<OrderItemEntity> createOrderItems(Set<CartItemEntity> cartItems, OrderEntity order) {
        Set<OrderItemEntity> orderItems = new HashSet<>();
        
        for (CartItemEntity cartItem : cartItems) {
            OrderItemEntity orderItem = new OrderItemEntity();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getVariant().getProduct());
            orderItem.setVariant(cartItem.getVariant());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getVariant().getPrice());
            orderItem.setTotalPrice(cartItem.getVariant().getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            
            orderItems.add(orderItemRepository.save(orderItem));
        }
        
        return orderItems;
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Validar transiciones de estado permitidas
        switch (currentStatus) {
            case PENDIENTE:
                if (newStatus != OrderStatus.PAGADO && newStatus != OrderStatus.CANCELADO) {
                    throw new IllegalStateException("Transición de estado no válida");
                }
                break;
            case PAGADO:
                if (newStatus != OrderStatus.PREPARANDO && newStatus != OrderStatus.CANCELADO) {
                    throw new IllegalStateException("Transición de estado no válida");
                }
                break;
            case PREPARANDO:
                if (newStatus != OrderStatus.ENVIADO && newStatus != OrderStatus.CANCELADO) {
                    throw new IllegalStateException("Transición de estado no válida");
                }
                break;
            case ENVIADO:
                if (newStatus != OrderStatus.ENTREGADO && newStatus != OrderStatus.CANCELADO) {
                    throw new IllegalStateException("Transición de estado no válida");
                }
                break;
            case ENTREGADO:
            case CANCELADO:
                throw new IllegalStateException("No se puede cambiar el estado de una orden entregada o cancelada");
            default:
                throw new IllegalStateException("Estado de orden no válido");
        }
    }

    private OrderDto mapToOrderDto(OrderEntity order) {
        return OrderDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUser().getId())
                .userName(order.getUser().getName())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .createdBy(order.getCreatedBy())
                .updatedBy(order.getUpdatedBy())
                .items(mapToOrderItemDtos(order.getItems()))
                .build();
    }

    private Set<OrderItemDto> mapToOrderItemDtos(Set<OrderItemEntity> items) {
        return items.stream()
                .map(item -> OrderItemDto.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .variantId(item.getVariant().getId())
                        .variantSku(item.getVariant().getSku())
                        .variantColor(item.getVariant().getColorName())
                        .variantSize(item.getVariant().getSize().toString())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getTotalPrice())
                        .build())
                .collect(Collectors.toSet());
    }
}