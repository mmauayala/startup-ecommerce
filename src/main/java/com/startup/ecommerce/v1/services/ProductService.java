package com.startup.ecommerce.v1.services;

import com.startup.ecommerce.v1.dto.ProductDto;
import java.util.List;

public interface ProductService {
    
    ProductDto createProduct(ProductDto productDto);
    ProductDto updateProduct(Long id, ProductDto productDto);
    
    void deleteProduct(Long id);
    
    ProductDto getProductById(Long id);
    
    List<ProductDto> getAllProducts();
    
    List<ProductDto> getFeaturedProducts();
    
    List<ProductDto> searchProducts(
        String search,
        String category,
        String size,
        String color,
        Double minPrice,
        Double maxPrice,
        String sort,
        int page,
        int sizePage
    );
}
