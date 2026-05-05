package com.silvestre_lanchonete.order_service.infra.mapper;

import com.silvestre_lanchonete.order_service.domain.Order;
import com.silvestre_lanchonete.order_service.dto.OrderProductResponseDTO;
import com.silvestre_lanchonete.order_service.dto.OrderResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public OrderResponseDTO toResponseDTO(Order order) {
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