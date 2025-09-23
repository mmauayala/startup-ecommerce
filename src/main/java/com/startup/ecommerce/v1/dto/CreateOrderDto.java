package com.startup.ecommerce.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateOrderDto {
    
    @NotBlank(message = "La dirección de envío es obligatoria")
    @Size(max = 255, message = "La dirección de envío no puede exceder 255 caracteres")
    private String shippingAddress;
    
    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    private String notes;
    
}