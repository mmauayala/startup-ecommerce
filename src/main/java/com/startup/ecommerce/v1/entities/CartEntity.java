package com.startup.ecommerce.v1.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "cart")
public class CartEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<CartItemEntity> items = new LinkedHashSet<>();

    @Column(name = "total_price", nullable = false)
    @Builder.Default
    private Double totalPrice = 0.0;

    public void addItem(CartItemEntity item) {
        if (items == null) {
            items = new LinkedHashSet<>();
        }
        item.setCart(this);
        items.add(item);
    }

    public void removeItem(CartItemEntity item) {
        if (items != null) {
            items.remove(item);
            item.setCart(null);
        }
    }
}


