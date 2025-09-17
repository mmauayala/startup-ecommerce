package com.startup.ecommerce.v1.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDto {
    private Long id;
    private String name;
    private Double price;
    private CategorySimpleDto category;
    private String image;
    private Integer stock;
    private java.util.List<ProductVariantDto> variants;
}
