package com.silvestre_lanchonete.order_service.controller;

import com.silvestre_lanchonete.order_service.dto.OrderRequestDTO;
import com.silvestre_lanchonete.order_service.dto.OrderResponseDTO;
import com.silvestre_lanchonete.order_service.domain.Order;
import com.silvestre_lanchonete.order_service.infra.mapper.OrderMapper;
import com.silvestre_lanchonete.order_service.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@Tag(name = "Pedidos", description = "Gerenciamento de pedidos da lanchonete")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    public OrderController(OrderService orderService, OrderMapper orderMapper) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
    }

    @PostMapping
    @Operation(summary = "Criar pedido")
    @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    public ResponseEntity<OrderResponseDTO> createOrder(
            @Valid @RequestBody OrderRequestDTO orderRequest,
            @AuthenticationPrincipal String emailClient) {

        Order newOrder = orderService.createOrder(orderRequest, emailClient);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderMapper.toResponseDTO(newOrder));
    }

    @GetMapping
    @Operation(summary = "Listar pedidos do usuário com paginação")
    public ResponseEntity<Page<OrderResponseDTO>> getAllOrders(
            @AuthenticationPrincipal String emailClient,
            @Parameter(description = "Filtrar por status: PENDENTE, CONFIRMADO, CANCELADO, CONCLUIDO")
            @RequestParam(required = false) Order.OrderStatus status,
            @Parameter(description = "Página (começa em 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Itens por página (máximo 50)")
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 50),
                Sort.by("createdAt").descending());

        Page<OrderResponseDTO> orders = orderService
                .getAllOrders(emailClient, status, pageable)
                .map(orderMapper::toResponseDTO);

        return ResponseEntity.ok(orders);
    }

    @PutMapping("/status/{id}")
    @Transactional
    @Operation(summary = "Atualizar status do pedido", description = "Requer role Administrador")
    @ApiResponse(responseCode = "200", description = "Status atualizado")
    @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable UUID id,
            @RequestParam Order.OrderStatus newStatus) {

        Order updated = orderService.updateOrderStatus(id, newStatus);
        return ResponseEntity.ok(orderMapper.toResponseDTO(updated));
    }
}