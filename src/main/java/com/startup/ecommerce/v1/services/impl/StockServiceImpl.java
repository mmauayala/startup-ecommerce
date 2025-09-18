package com.startup.ecommerce.v1.services.impl;

import com.startup.ecommerce.v1.entities.ProductVariantEntity;
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
    public Integer getAvailableStock(Long variantId) {
        return stockRepository.findByVariantId(variantId)
                .map(StockEntity::getAvailable)
                .orElse(0);
    }

    @Override
    public Integer getTotalStock(Long variantId) {
        return stockRepository.findByVariantId(variantId)
                .map(StockEntity::getQuantity)
                .orElse(0);
    }

    @Override
    @Transactional
    public StockEntity createStock(ProductVariantEntity variant, Integer quantity) {
        StockEntity stock = StockEntity.builder()
                .variant(variant)
                .quantity(quantity)
                .reserved(0)
                .available(quantity)
                .build();
        return stockRepository.save(stock);
    }

    @Override
    @Transactional
    public StockEntity updateStock(Long variantId, Integer quantity) {
        StockEntity stock = stockRepository.findByVariantId(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Stock no encontrado para la variante: " + variantId));
        stock.setQuantity(quantity);
        return stockRepository.save(stock);
    }

    @Override
    @Transactional
    public void reserveStock(Long variantId, Integer quantity) {
        StockEntity stock = stockRepository.findByVariantId(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Stock no encontrado para la variante: " + variantId));
        
        if (stock.getAvailable() < quantity) {
            throw new IllegalArgumentException("Stock insuficiente. Disponible: " + stock.getAvailable() + ", Solicitado: " + quantity);
        }
        
        stock.setReserved(stock.getReserved() + quantity);
        stockRepository.save(stock);
    }

    @Override
    @Transactional
    public void releaseStock(Long variantId, Integer quantity) {
        StockEntity stock = stockRepository.findByVariantId(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Stock no encontrado para la variante: " + variantId));
        
        if (stock.getReserved() < quantity) {
            throw new IllegalArgumentException("No se puede liberar más stock del reservado. Reservado: " + stock.getReserved() + ", Solicitado: " + quantity);
        }
        
        stock.setReserved(stock.getReserved() - quantity);
        stockRepository.save(stock);
    }

    @Override
    @Transactional
    public void consumeStock(Long variantId, Integer quantity) {
        StockEntity stock = stockRepository.findByVariantId(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Stock no encontrado para la variante: " + variantId));
        
        if (stock.getAvailable() < quantity) {
            throw new IllegalArgumentException("Stock insuficiente. Disponible: " + stock.getAvailable() + ", Solicitado: " + quantity);
        }
        
        stock.setQuantity(stock.getQuantity() - quantity);
        stockRepository.save(stock);
    }
}
