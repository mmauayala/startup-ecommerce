package com.startup.ecommerce.v1.services;

import com.startup.ecommerce.v1.entities.ProductVariantEntity;
import com.startup.ecommerce.v1.entities.StockEntity;

public interface StockService {

    
    Integer getAvailableStock(Long variantId);
    
    Integer getTotalStock(Long variantId);
    
    StockEntity createStock(ProductVariantEntity variant, Integer quantity);
    
    StockEntity updateStock(Long variantId, Integer quantity);
    
    void reserveStock(Long variantId, Integer quantity);
    
    void releaseStock(Long variantId, Integer quantity);
    
    void consumeStock(Long variantId, Integer quantity);
}
