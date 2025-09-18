package com.startup.ecommerce.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCategoryDto {
    @NotBlank
    @Size(max = 50, message = "El nombre de la categoría no puede exceder 50 caracteres")
    private String name;
}
