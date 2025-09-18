package com.startup.ecommerce.v1.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stock")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false, unique = true)
    private ProductVariantEntity variant;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Builder.Default
    @Column(nullable = false)
    private Integer reserved = 0;
    
    @Column(nullable = false)
    private Integer available;
    
    @PrePersist
    @PreUpdate
    private void calculateAvailable() {
        this.available = this.quantity - this.reserved;
    }
}
