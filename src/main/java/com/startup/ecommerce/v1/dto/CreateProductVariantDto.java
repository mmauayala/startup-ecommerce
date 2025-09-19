package com.startup.ecommerce.v1.dto;

import com.startup.ecommerce.v1.entities.enums.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateProductVariantDto {

    @NotNull
    private Size size;

    @NotBlank
    @jakarta.validation.constraints.Size(max = 40)
    private String colorName;

    @NotBlank
    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "colorHex debe ser formato #RRGGBB")
    private String colorHex;

    @jakarta.validation.constraints.Size(max = 60)
    private String sku; // opcional; si no llega se genera
}
