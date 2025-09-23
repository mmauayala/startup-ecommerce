package com.startup.ecommerce.v1.entities;

import jakarta.persistence.*;
import lombok.*;
import com.startup.ecommerce.v1.entities.enums.Size;
import java.math.BigDecimal;

@Entity
@Table(name = "product_variant",
       uniqueConstraints = @UniqueConstraint(name = "uk_product_variant_unique", columnNames = {"product_id","size","color_hex"}))
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Size size; // XS, S, M, L, XL, XXL

    @Column(nullable = false)
    private String colorName;

    @Column(name = "color_hex", nullable = false)
    private String colorHex;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
}
