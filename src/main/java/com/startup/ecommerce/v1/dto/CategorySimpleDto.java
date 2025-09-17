package com.startup.ecommerce.v1.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategorySimpleDto {
    private Long id;
    private String name;
}
