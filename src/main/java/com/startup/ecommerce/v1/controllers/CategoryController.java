package com.startup.ecommerce.v1.controllers;

import com.startup.ecommerce.v1.dto.CategorySimpleDto;
import com.startup.ecommerce.v1.dto.CreateCategoryDto;
import com.startup.ecommerce.v1.entities.CategoryEntity;
import com.startup.ecommerce.v1.repositories.CategoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categorías", description = """
    Gestión de categorías de productos.
    
    Las categorías son utilizadas para organizar los productos y facilitar su búsqueda.
    Solo los administradores pueden crear, modificar o eliminar categorías.
    Las categorías son públicas y pueden ser consultadas sin autenticación.
    """)
public class CategoryController {
    private final CategoryRepository categoryRepository;

    @Operation(summary = "Crear categoría", responses = {
        @ApiResponse(responseCode = "201", description = "Categoría creada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategorySimpleDto.class))),
        @ApiResponse(responseCode = "400", description = "Nombre de categoría inválido o ya existe")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CategorySimpleDto> createCategory(@Valid @RequestBody CreateCategoryDto dto) {
        // Verificar si ya existe una categoría con ese nombre
        if (categoryRepository.findByNameIgnoreCase(dto.getName()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        
        CategoryEntity category = new CategoryEntity();
        category.setName(dto.getName());
        CategoryEntity saved = categoryRepository.save(category);
        
        CategorySimpleDto response = new CategorySimpleDto(saved.getId(), saved.getName());
        return ResponseEntity.status(201).body(response);
    }
}
