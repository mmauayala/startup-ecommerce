package com.startup.ecommerce.v1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDto {
    
    private Long id;
    
    private Long productId;
    
    private String productName;
    
    private Long variantId;
    
    private String variantSku;
    
    private String variantColor;
    
    private String variantSize;
    
    private Integer quantity;
    
    private BigDecimal unitPrice;
    
    private BigDecimal totalPrice;

}
