package com.silvestre_lanchonete.api.controller;

import com.silvestre_lanchonete.api.DTO.OrderRequestDTO;
import com.silvestre_lanchonete.api.DTO.OrderResponseDTO;
import com.silvestre_lanchonete.api.domain.order.Order;
import com.silvestre_lanchonete.api.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderContoller {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody OrderRequestDTO orderRequest) {
        Order newOrder = orderService.createOrder(orderRequest);

        OrderResponseDTO responseDTO = new OrderResponseDTO(
                newOrder.getId(),
                newOrder.getStatus().name(),
                newOrder.getTotal(),
                newOrder.getOrderProducts().stream().map(op -> new OrderResponseDTO.OrderProductResponse(
                        op.getProduct().getId(),
                        op.getProduct().getName(),
                        op.getAmount(),
                        op.getPrice()
                )).toList()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable UUID id,
            @RequestParam Order.OrderStatus newStatus) {

        Order updatedOrder = orderService.updateOrderStatus(id, newStatus);

        List<OrderResponseDTO.OrderProductResponse> products = updatedOrder.getOrderProducts().stream()
                .map(op -> new OrderResponseDTO.OrderProductResponse(
                        op.getProduct().getId(),
                        op.getProduct().getName(),
                        op.getAmount(),
                        op.getPrice()
                )).toList();

        OrderResponseDTO responseDTO = new OrderResponseDTO(
                updatedOrder.getId(),
                updatedOrder.getStatus().name(),
                updatedOrder.getTotal(),
                products
        );

        return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders(@AuthenticationPrincipal UserDetails userDetails) {
        String emailClient = userDetails.getUsername();
        List<Order> orders = orderService.getAllOrders(emailClient);

        List<OrderResponseDTO> responseDTOList = orders.stream().map(order -> new OrderResponseDTO(
                order.getId(),
                order.getStatus().name(),
                order.getTotal(),
                order.getOrderProducts().stream().map(op -> new OrderResponseDTO.OrderProductResponse(
                        op.getProduct().getId(),
                        op.getProduct().getName(),
                        op.getAmount(),
                        op.getPrice()
                )).toList()
        )).toList();

        return ResponseEntity.status(HttpStatus.OK).body(responseDTOList);
    }
}
