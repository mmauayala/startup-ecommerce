package com.startup.ecommerce.v1.repositories;

import com.startup.ecommerce.v1.entities.OrderEntity;
import com.startup.ecommerce.v1.entities.UserEntity;
import com.startup.ecommerce.v1.entities.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    
    Optional<OrderEntity> findByOrderNumber(String orderNumber);
    
    Page<OrderEntity> findByUser(UserEntity user, Pageable pageable);
    
    Page<OrderEntity> findByStatus(OrderStatus status, Pageable pageable);
    
    Page<OrderEntity> findByUserAndStatus(UserEntity user, OrderStatus status, Pageable pageable);
}