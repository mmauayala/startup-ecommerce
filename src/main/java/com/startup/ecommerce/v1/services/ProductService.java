package com.startup.ecommerce.v1.services;

import com.startup.ecommerce.v1.dto.ProductDto;
import com.startup.ecommerce.v1.dto.ProductVariantDto;
import com.startup.ecommerce.v1.dto.CreateProductVariantDto;
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

    // Variants
    ProductVariantDto addVariant(Long productId, CreateProductVariantDto dto);
    List<ProductVariantDto> listVariants(Long productId);
    ProductVariantDto updateVariant(Long productId, Long variantId, CreateProductVariantDto dto);
    void deleteVariant(Long productId, Long variantId);
}
