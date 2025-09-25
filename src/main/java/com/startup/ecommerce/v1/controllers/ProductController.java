package com.startup.ecommerce.v1.controllers;

import com.startup.ecommerce.v1.dto.ProductDto;
import com.startup.ecommerce.v1.services.ProductService;
import com.startup.ecommerce.v1.dto.CreateProductVariantDto;
import com.startup.ecommerce.v1.dto.ProductVariantDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Productos", description = """
    Gestión completa de productos y sus variantes.
    
    Funcionalidades principales:
    - Búsqueda avanzada con filtros
    - Gestión de productos destacados
    - CRUD completo de productos (solo admin)
    - Gestión de variantes (tallas, colores)
    - Control de stock
    
    Los endpoints públicos son:
    - Listado de productos
    - Búsqueda con filtros
    - Detalle de producto
    - Productos destacados
    
    El resto requiere autenticación ADMIN.
    """)
public class ProductController {
    private final ProductService productService;

    @Operation(summary = "Obtener todos los productos", responses = {
        @ApiResponse(responseCode = "200", description = "Listado de productos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDto.class)))
    })
    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @Operation(
        summary = "Buscar productos con filtros avanzados",
        description = """
            Búsqueda avanzada de productos con múltiples criterios:
            
            Filtros disponibles:
            - search: Búsqueda por nombre o descripción
            - category: Filtrar por categoría
            - size: Filtrar por talla (XS, S, M, L, XL, XXL)
            - color: Filtrar por color
            - minPrice/maxPrice: Rango de precios
            
            Ordenamiento (parámetro sort):
            - price_asc: Precio ascendente
            - price_desc: Precio descendente
            - name_asc: Nombre A-Z
            - name_desc: Nombre Z-A
            - created_desc: Más recientes primero
            
            Paginación:
            - page: Número de página (0-based)
            - sizePage: Elementos por página
            """,
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Listado filtrado y paginado de productos",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDto.class))
            )
        }
    )
    @GetMapping("/search")
    public ResponseEntity<List<ProductDto>> searchProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String size,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int sizePage
    ) {
        return ResponseEntity.ok(productService.searchProducts(search, category, size, color, minPrice, maxPrice, sort, page, sizePage));
    }

    @Operation(summary = "Obtener producto por ID", responses = {
        @ApiResponse(responseCode = "200", description = "Producto encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDto.class))),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        ProductDto dto = productService.getProductById(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Obtener productos destacados", responses = {
        @ApiResponse(responseCode = "200", description = "Listado de destacados", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDto.class)))
    })
    @GetMapping("/featured")
    public ResponseEntity<List<ProductDto>> getFeaturedProducts() {
        return ResponseEntity.ok(productService.getFeaturedProducts());
    }

    @Operation(summary = "Crear producto", responses = {
        @ApiResponse(responseCode = "201", description = "Producto creado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDto.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto productDto) {
        return ResponseEntity.status(201).body(productService.createProduct(productDto));
    }

    @Operation(summary = "Actualizar producto", responses = {
        @ApiResponse(responseCode = "200", description = "Producto actualizado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDto.class))),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, @RequestBody ProductDto productDto) {
        ProductDto updated = productService.updateProduct(id, productDto);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Eliminar producto", responses = {
        @ApiResponse(responseCode = "204", description = "Producto eliminado"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Agregar variante a un producto")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{productId}/variants")
    public ResponseEntity<ProductVariantDto> addVariant(@PathVariable Long productId, @Valid @RequestBody CreateProductVariantDto dto) {
        return ResponseEntity.status(201).body(productService.addVariant(productId, dto));
    }

    @Operation(summary = "Listar variantes de un producto")
    @GetMapping("/{productId}/variants")
    public ResponseEntity<List<ProductVariantDto>> listVariants(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.listVariants(productId));
    }

    @Operation(summary = "Actualizar una variante")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{productId}/variants/{variantId}")
    public ResponseEntity<ProductVariantDto> updateVariant(@PathVariable Long productId, @PathVariable Long variantId, @Valid @RequestBody CreateProductVariantDto dto) {
        return ResponseEntity.ok(productService.updateVariant(productId, variantId, dto));
    }

    @Operation(summary = "Eliminar una variante")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{productId}/variants/{variantId}")
    public ResponseEntity<Void> deleteVariant(@PathVariable Long productId, @PathVariable Long variantId) {
        productService.deleteVariant(productId, variantId);
        return ResponseEntity.noContent().build();
    }
}
