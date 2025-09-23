package com.startup.ecommerce.v1.dto;

import com.startup.ecommerce.v1.entities.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {

    private Long id;

    private String orderNumber;
    
    private Long userId;
    
    private String userName;
    
    private OrderStatus status;
    
    private BigDecimal totalAmount;
    
    private String shippingAddress;
    
    private String notes;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private String createdBy;
    
    private String updatedBy;
    
    private Set<OrderItemDto> items;

}