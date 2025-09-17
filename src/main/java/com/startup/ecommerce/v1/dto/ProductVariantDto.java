package com.startup.ecommerce.v1.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductVariantDto {
    private Long id;
    private String size;
    private String colorName;
    private String colorHex;
    private String sku;
    private Integer stock;
}
