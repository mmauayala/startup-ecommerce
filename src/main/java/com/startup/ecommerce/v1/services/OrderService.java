package com.startup.ecommerce.v1.services;

import com.startup.ecommerce.v1.dto.CreateOrderDto;
import com.startup.ecommerce.v1.dto.OrderDto;
import com.startup.ecommerce.v1.entities.enums.OrderStatus;

import java.util.List;

public interface OrderService {
    
    OrderDto createOrder(Long userId, CreateOrderDto createOrderDto);

    OrderDto getOrderById(Long orderId);

    List<OrderDto> getOrdersByUserId(Long userId);

    OrderDto updateOrderStatus(Long orderId, OrderStatus status);

    OrderDto cancelOrder(Long orderId);
}