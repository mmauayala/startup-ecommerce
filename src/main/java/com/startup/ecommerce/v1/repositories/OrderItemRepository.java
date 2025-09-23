package com.startup.ecommerce.v1.repositories;

import com.startup.ecommerce.v1.entities.OrderEntity;
import com.startup.ecommerce.v1.entities.OrderItemEntity;
import com.startup.ecommerce.v1.entities.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {
    
    List<OrderItemEntity> findByOrder(OrderEntity order);
    
    List<OrderItemEntity> findByProduct(ProductEntity product);
}