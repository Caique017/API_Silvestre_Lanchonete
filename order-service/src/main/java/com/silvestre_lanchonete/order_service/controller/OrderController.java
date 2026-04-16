package com.silvestre_lanchonete.order_service.controller;

import com.silvestre_lanchonete.order_service.dto.OrderProductResponseDTO;
import com.silvestre_lanchonete.order_service.dto.OrderRequestDTO;
import com.silvestre_lanchonete.order_service.dto.OrderResponseDTO;
import com.silvestre_lanchonete.order_service.domain.Order;
import com.silvestre_lanchonete.order_service.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(
            @RequestBody OrderRequestDTO orderRequest,
            @AuthenticationPrincipal String emailClient,
            @RequestHeader("Authorization") String token) {

        Order newOrder = orderService.createOrder(orderRequest, emailClient, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponseDTO(newOrder));
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable UUID id,
            @RequestParam Order.OrderStatus newStatus) {

        Order updatedOrder = orderService.updateOrderStatus(id, newStatus);
        return ResponseEntity.status(HttpStatus.OK).body(mapToResponseDTO(updatedOrder));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders(
            @AuthenticationPrincipal String emailClient
           ) {

        List<Order> orders = orderService.getAllOrders(emailClient);

        List<OrderResponseDTO> responseDTOList = orders.stream()
                .map(this::mapToResponseDTO)
                .toList();

        return ResponseEntity.status(HttpStatus.OK).body(responseDTOList);
    }

    private OrderResponseDTO mapToResponseDTO(Order order) {
        List<OrderProductResponseDTO> items = order.getOrderProducts().stream()
                .map(op -> new OrderProductResponseDTO(
                        op.getProductId(),
                        op.getAmount(),
                        op.getPrice()
                )).toList();

        return new OrderResponseDTO(
                order.getId(),
                order.getStatus().name(),
                order.getTotal(),
                items
        );
    }
}