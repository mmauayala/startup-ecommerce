package com.startup.ecommerce.v1.controllers;

import com.startup.ecommerce.v1.dto.CreateOrderDto;
import com.startup.ecommerce.v1.dto.OrderDto;
import com.startup.ecommerce.v1.entities.enums.OrderStatus;
import com.startup.ecommerce.v1.entities.UserEntity;
import com.startup.ecommerce.v1.entities.enums.Role;
import com.startup.ecommerce.v1.services.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = """
    Endpoints para gestión de órdenes de compra.
    
    Flujo de estados de una orden:
    PENDIENTE → PAGADO → PREPARANDO → ENVIADO → ENTREGADO
    (En cualquier momento puede pasar a CANCELADO)
    
    Roles y permisos:
    - ADMIN: Puede ver y gestionar todas las órdenes
    - CLIENTE: Solo puede gestionar sus propias órdenes
    """)
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(
        summary = "Crear una nueva orden",
        description = """
            Crea una nueva orden a partir del carrito actual del usuario.
            
            Proceso:
            1. Valida que el carrito no esté vacío
            2. Crea la orden con los items del carrito
            3. Reserva el stock de los productos
            4. Limpia el carrito
            
            La orden se crea en estado PENDIENTE.
            """)
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<OrderDto> createOrder(
            @AuthenticationPrincipal UserEntity user,
            @Valid @RequestBody CreateOrderDto createOrderDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(user.getId(), createOrderDto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una orden por ID", description = "Obtiene los detalles de una orden específica")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<OrderDto> getOrder(
            @Parameter(description = "ID de la orden") @PathVariable Long id,
            @AuthenticationPrincipal UserEntity user) {
        OrderDto order = orderService.getOrderById(id);
        // Solo ADMIN puede ver cualquier orden, CLIENTE solo las suyas
        if (user.getRole() != Role.ADMIN && !order.getUserId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(order);
    }

    @GetMapping("/my-orders")
    @Operation(summary = "Listar mis órdenes", description = "Obtiene todas las órdenes del usuario autenticado")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<List<OrderDto>> getMyOrders(
            @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(orderService.getOrdersByUserId(user.getId()));
    }

    @PatchMapping("/{id}/status")
    @Operation(
        summary = "Actualizar estado de orden", 
        description = """
            Actualiza el estado de una orden específica. Solo administradores pueden usar este endpoint.
            
            Transiciones permitidas:
            - PENDIENTE → PAGADO, CANCELADO
            - PAGADO → PREPARANDO, CANCELADO
            - PREPARANDO → ENVIADO, CANCELADO
            - ENVIADO → ENTREGADO, CANCELADO
            - ENTREGADO → (estado final)
            - CANCELADO → (estado final)
            
            Efectos:
            - Al marcar como ENTREGADO: se consume el stock reservado
            - Al marcar como CANCELADO: se libera el stock reservado
            """)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @Parameter(description = "ID de la orden") @PathVariable Long id,
            @Parameter(description = "Nuevo estado de la orden") @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancelar una orden", description = "Cancela una orden específica")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<OrderDto> cancelOrder(
            @Parameter(description = "ID de la orden") @PathVariable Long id,
            @AuthenticationPrincipal UserEntity user) {
        OrderDto order = orderService.getOrderById(id);
        // Solo ADMIN puede cancelar cualquier orden, CLIENTE solo las suyas
        if (user.getRole() != Role.ADMIN && !order.getUserId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }
}