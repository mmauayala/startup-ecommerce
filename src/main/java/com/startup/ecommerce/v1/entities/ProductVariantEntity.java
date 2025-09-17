package com.startup.ecommerce.v1.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_variant")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductVariantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(nullable = false)
    private String size; // XS, S, M, L, XL, XXL

    @Column(nullable = false)
    private String colorName;

    @Column(nullable = false)
    private String colorHex;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private Integer stock;
}
