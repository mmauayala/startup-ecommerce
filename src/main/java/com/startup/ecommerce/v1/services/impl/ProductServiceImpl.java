package com.startup.ecommerce.v1.services.impl;

import com.startup.ecommerce.v1.dto.ProductDto;
import com.startup.ecommerce.v1.dto.CategorySimpleDto;
import com.startup.ecommerce.v1.entities.ProductEntity;
import com.startup.ecommerce.v1.entities.CategoryEntity;
import com.startup.ecommerce.v1.repositories.ProductRepository;
import com.startup.ecommerce.v1.repositories.ProductVariantRepository;
import com.startup.ecommerce.v1.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    public ProductDto createProduct(ProductDto productDto) {
        ProductEntity entity = toEntity(productDto);
        ProductEntity saved = productRepository.save(entity);
        return toDto(saved);
    }

    @Override
    public ProductDto updateProduct(Long id, ProductDto productDto) {
        ProductEntity entity = productRepository.findById(id).orElseThrow();
        entity.setName(productDto.getName());
        entity.setPrice(productDto.getPrice());
        entity.setImage(productDto.getImage());
        entity.setStock(productDto.getStock());
        // Falta lógica para actualizar categoría correctamente
        ProductEntity updated = productRepository.save(entity);
        return toDto(updated);
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public ProductDto getProductById(Long id) {
        return productRepository.findById(id).map(this::toDto).orElse(null);
    }

    @Override
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<ProductDto> getFeaturedProducts() {
        return productRepository.findByFeaturedTrue().stream().map(this::toDto).toList();
    }

    @Override
    public List<ProductDto> searchProducts(String search, String category, String size, String color, Double minPrice, Double maxPrice, String sort, int page, int sizePage) {
        // Construcción de filtros dinámicos
        Pageable pageable = PageRequest.of(page, sizePage, sort != null && sort.equals("price_asc") ? Sort.by("price").ascending() : Sort.by("price").descending());
        // Filtro base: por categoría y texto
        List<ProductEntity> products = productRepository.findAll().stream()
            .filter(p -> (category == null || p.getCategory().getName().equalsIgnoreCase(category)))
            .filter(p -> (search == null || p.getName().toLowerCase().contains(search.toLowerCase())))
            .filter(p -> (minPrice == null || p.getPrice() >= minPrice))
            .filter(p -> (maxPrice == null || p.getPrice() <= maxPrice))
            .toList();
        // Filtro por variantes (talla y color)
        if (size != null || color != null) {
            products = products.stream().filter(p ->
                p.getVariants() != null && p.getVariants().stream().anyMatch(v ->
                    (size == null || v.getSize().equalsIgnoreCase(size)) &&
                    (color == null || v.getColorName().equalsIgnoreCase(color))
                )
            ).toList();
        }
        // Paginación manual (ya que usamos stream)
        int from = Math.min(page * sizePage, products.size());
        int to = Math.min(from + sizePage, products.size());
        List<ProductEntity> paged = products.subList(from, to);
        return paged.stream().map(this::toDto).toList();
    }

    private ProductDto toDto(ProductEntity entity) {
        return ProductDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .price(entity.getPrice())
                .image(entity.getImage())
                .stock(entity.getStock())
                .category(entity.getCategory() != null ? new CategorySimpleDto(entity.getCategory().getId(), entity.getCategory().getName()) : null)
                .variants(entity.getVariants() != null ? entity.getVariants().stream().map(v -> com.startup.ecommerce.v1.dto.ProductVariantDto.builder()
                        .id(v.getId())
                        .size(v.getSize())
                        .colorName(v.getColorName())
                        .colorHex(v.getColorHex())
                        .sku(v.getSku())
                        .stock(v.getStock())
                        .build()).toList() : java.util.List.of())
                .build();
    }

    private ProductEntity toEntity(ProductDto dto) {
        ProductEntity entity = new ProductEntity();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setPrice(dto.getPrice());
        entity.setImage(dto.getImage());
        entity.setStock(dto.getStock());
        
        return entity;
    }
}
