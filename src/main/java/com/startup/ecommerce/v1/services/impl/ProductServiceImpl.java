package com.startup.ecommerce.v1.services.impl;

import com.startup.ecommerce.v1.dto.ProductDto;
import com.startup.ecommerce.v1.dto.CategorySimpleDto;
import com.startup.ecommerce.v1.dto.CreateProductVariantDto;
import com.startup.ecommerce.v1.dto.ProductVariantDto;
import com.startup.ecommerce.v1.entities.ProductEntity;
import com.startup.ecommerce.v1.entities.ProductVariantEntity;
import com.startup.ecommerce.v1.repositories.ProductRepository;
import com.startup.ecommerce.v1.repositories.ProductVariantRepository;
import com.startup.ecommerce.v1.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import com.startup.ecommerce.v1.entities.enums.Size;

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
        // Filtro base en memoria (a optimizar con Specifications/JPQL más adelante)
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
                    (size == null || v.getSize().name().equalsIgnoreCase(size)) &&
                    (color == null || v.getColorName().equalsIgnoreCase(color))
                )
            ).toList();
        }
        // Orden básico por precio
        if ("price_asc".equalsIgnoreCase(sort)) {
            products = products.stream().sorted(java.util.Comparator.comparing(ProductEntity::getPrice)).toList();
        } else if ("price_desc".equalsIgnoreCase(sort)) {
            products = products.stream().sorted(java.util.Comparator.comparing(ProductEntity::getPrice).reversed()).toList();
        }
        // Paginación manual
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

    private void recalculateProductStock(ProductEntity product) {
        int sum = product.getVariants() == null ? 0 : product.getVariants().stream().mapToInt(ProductVariantEntity::getStock).sum();
        product.setStock(sum);
        productRepository.save(product);
    }

    private String generateSku(ProductEntity product, String size, String colorHex) {
        String normalizedSize = size.toUpperCase();
        String normalizedColor = colorHex.replace("#", "").toUpperCase();
        String base = "PROD-" + product.getId() + "-" + normalizedSize + "-" + normalizedColor;
        long count = product.getVariants() == null ? 0 : product.getVariants().size();
        return base + "-" + String.format("%03d", count + 1);
    }

    @Override
    public ProductVariantDto addVariant(Long productId, CreateProductVariantDto dto) {
        ProductEntity product = productRepository.findById(productId).orElseThrow();
        Size size = dto.getSize();
        String colorHex = dto.getColorHex().toUpperCase();
        if (productVariantRepository.existsByProductAndSizeAndColorHexIgnoreCase(product, size, colorHex)) {
            throw new IllegalArgumentException("Ya existe una variante con ese tamaño y color para este producto");
        }
        ProductVariantEntity variant = ProductVariantEntity.builder()
                .product(product)
                .size(size)
                .colorName(dto.getColorName())
                .colorHex(colorHex)
                .sku(dto.getSku() != null && !dto.getSku().isBlank() ? dto.getSku() : generateSku(product, size.name(), colorHex))
                .stock(dto.getStock())
                .build();
        ProductVariantEntity saved = productVariantRepository.save(variant);
        if (product.getVariants() != null) product.getVariants().add(saved);
        recalculateProductStock(product);
        return ProductVariantDto.builder()
                .id(saved.getId())
                .size(saved.getSize())
                .colorName(saved.getColorName())
                .colorHex(saved.getColorHex())
                .sku(saved.getSku())
                .stock(saved.getStock())
                .build();
    }

    @Override
    public List<ProductVariantDto> listVariants(Long productId) {
        ProductEntity product = productRepository.findById(productId).orElseThrow();
        return (product.getVariants() == null ? java.util.Set.<ProductVariantEntity>of() : product.getVariants())
                .stream()
                .map(v -> ProductVariantDto.builder()
                        .id(v.getId())
                        .size(v.getSize())
                        .colorName(v.getColorName())
                        .colorHex(v.getColorHex())
                        .sku(v.getSku())
                        .stock(v.getStock())
                        .build())
                .toList();
    }

    @Override
    public ProductVariantDto updateVariant(Long productId, Long variantId, CreateProductVariantDto dto) {
        ProductEntity product = productRepository.findById(productId).orElseThrow();
        ProductVariantEntity variant = productVariantRepository.findById(variantId).orElseThrow();
        if (!variant.getProduct().getId().equals(product.getId())) {
            throw new IllegalArgumentException("La variante no pertenece al producto indicado");
        }
        Size size = dto.getSize();
        String colorHex = dto.getColorHex().toUpperCase();
        if (variant.getSize() != size || !variant.getColorHex().equalsIgnoreCase(colorHex)) {
            if (productVariantRepository.existsByProductAndSizeAndColorHexIgnoreCase(product, size, colorHex)) {
                throw new IllegalArgumentException("Ya existe una variante con ese tamaño y color para este producto");
            }
        }
        variant.setSize(size);
        variant.setColorName(dto.getColorName());
        variant.setColorHex(colorHex);
        variant.setStock(dto.getStock());
        if (dto.getSku() != null && !dto.getSku().isBlank()) {
            variant.setSku(dto.getSku());
        }
        ProductVariantEntity updated = productVariantRepository.save(variant);
        recalculateProductStock(product);
        return ProductVariantDto.builder()
                .id(updated.getId())
                .size(updated.getSize())
                .colorName(updated.getColorName())
                .colorHex(updated.getColorHex())
                .sku(updated.getSku())
                .stock(updated.getStock())
                .build();
    }

    @Override
    public void deleteVariant(Long productId, Long variantId) {
        ProductEntity product = productRepository.findById(productId).orElseThrow();
        ProductVariantEntity variant = productVariantRepository.findById(variantId).orElseThrow();
        if (!variant.getProduct().getId().equals(product.getId())) {
            throw new IllegalArgumentException("La variante no pertenece al producto indicado");
        }
        productVariantRepository.delete(variant);
        if (product.getVariants() != null) product.getVariants().removeIf(v -> v.getId().equals(variantId));
        recalculateProductStock(product);
    }
}
