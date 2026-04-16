package com.silvestre_lanchonete.order_service.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record OrderRequestDTO(@NotEmpty(message = "O pedido deve conter pelo menos um item")
                              List<OrderItemRequestDTO> items) {
}
