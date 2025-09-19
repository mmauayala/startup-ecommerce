package com.startup.ecommerce.v1.services;

import com.startup.ecommerce.v1.entities.ProductEntity;
import com.startup.ecommerce.v1.entities.StockEntity;

public interface StockService {
    
    Integer getAvailableStock(Long productId);
    
    Integer getTotalStock(Long productId);
    
    StockEntity createStock(ProductEntity product, Integer quantity);
    
    StockEntity updateStock(Long productId, Integer quantity);
    
    void reserveStock(Long productId, Integer quantity);
    
    void releaseStock(Long productId, Integer quantity);
    
    void consumeStock(Long productId, Integer quantity);
    
    void deleteStock(Long productId);
}
