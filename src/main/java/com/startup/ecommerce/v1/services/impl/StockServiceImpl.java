package com.startup.ecommerce.v1.services.impl;

import com.startup.ecommerce.v1.entities.ProductEntity;
import com.startup.ecommerce.v1.entities.StockEntity;
import com.startup.ecommerce.v1.repositories.StockRepository;
import com.startup.ecommerce.v1.services.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {
    private final StockRepository stockRepository;

    @Override
    public Integer getAvailableStock(Long productId) {
        return stockRepository.findByProductId(productId)
                .map(StockEntity::getAvailable)
                .orElse(0);
    }

    @Override
    public Integer getTotalStock(Long productId) {
        return stockRepository.findByProductId(productId)
                .map(StockEntity::getQuantity)
                .orElse(0);
    }

    @Override
    @Transactional
    public StockEntity createStock(ProductEntity product, Integer quantity) {
        if (stockRepository.findByProductId(product.getId()).isPresent()) {
            throw new IllegalArgumentException("Ya existe stock para este producto");
        }
        
        StockEntity stock = StockEntity.builder()
                .product(product)
                .quantity(quantity)
                .reserved(0)
                .build();
        return stockRepository.save(stock);
    }

    @Override
    @Transactional
    public StockEntity updateStock(Long productId, Integer quantity) {
        StockEntity stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Stock no encontrado para el producto: " + productId));
        
        if (quantity < stock.getReserved()) {
            throw new IllegalArgumentException("La nueva cantidad no puede ser menor que el stock reservado");
        }
        
        stock.setQuantity(quantity);
        return stockRepository.save(stock);
    }

    @Override
    @Transactional
    public void reserveStock(Long productId, Integer quantity) {
        StockEntity stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Stock no encontrado para el producto: " + productId));
        
        if (stock.getAvailable() < quantity) {
            throw new IllegalArgumentException("Stock insuficiente. Disponible: " + stock.getAvailable() + ", Solicitado: " + quantity);
        }
        
        stock.setReserved(stock.getReserved() + quantity);
        stockRepository.save(stock);
    }

    @Override
    @Transactional
    public void releaseStock(Long productId, Integer quantity) {
        StockEntity stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Stock no encontrado para el producto: " + productId));
        
        if (stock.getReserved() < quantity) {
            throw new IllegalArgumentException("No se puede liberar más stock del reservado. Reservado: " + stock.getReserved() + ", Solicitado: " + quantity);
        }
        
        stock.setReserved(stock.getReserved() - quantity);
        stockRepository.save(stock);
    }

    @Override
    @Transactional
    public void consumeStock(Long productId, Integer quantity) {
        StockEntity stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Stock no encontrado para el producto: " + productId));
        
        if (stock.getAvailable() < quantity) {
            throw new IllegalArgumentException("Stock insuficiente. Disponible: " + stock.getAvailable() + ", Solicitado: " + quantity);
        }
        
        stock.setQuantity(stock.getQuantity() - quantity);
        stockRepository.save(stock);
    }

    @Override
    @Transactional
    public void deleteStock(Long productId) {
        StockEntity stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Stock no encontrado para el producto: " + productId));
        stockRepository.delete(stock);
    }
}
