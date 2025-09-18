package com.startup.ecommerce.v1.dto;

import lombok.*;
import com.startup.ecommerce.v1.entities.enums.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductVariantDto {
    private Long id;
    private Size size;
    private String colorName;
    private String colorHex;
    private String sku;
    private Integer stock;
}
