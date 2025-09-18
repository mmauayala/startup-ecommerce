package com.startup.ecommerce.v1.repositories;

import com.startup.ecommerce.v1.entities.StockEntity;
import com.startup.ecommerce.v1.entities.ProductVariantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<StockEntity, Long> {

    Optional<StockEntity> findByVariant(ProductVariantEntity variant);
    
    Optional<StockEntity> findByVariantId(Long variantId);

}
