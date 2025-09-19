package com.startup.ecommerce.v1.repositories;

import com.startup.ecommerce.v1.entities.StockEntity;
import com.startup.ecommerce.v1.entities.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<StockEntity, Long> {

    Optional<StockEntity> findByProduct(ProductEntity product);
    
    Optional<StockEntity> findByProductId(Long productId);

}
