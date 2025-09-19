package com.startup.ecommerce.v1.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stock")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockEntity extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private ProductEntity product;
    
    @Column(nullable = false)
    private Integer quantity; // Stock total en almacén
    
    @Builder.Default
    @Column(nullable = false)
    private Integer reserved = 0; // Stock reservado (ej. en carritos)
    
    @Transient // No se mapea a la base de datos, se calcula dinámicamente
    public Integer getAvailable() {
        return quantity - reserved;
    }
}
