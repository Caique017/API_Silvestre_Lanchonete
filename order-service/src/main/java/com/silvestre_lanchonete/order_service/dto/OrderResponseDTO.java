package com.silvestre_lanchonete.order_service.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderResponseDTO(UUID id,
                               String status,
                               BigDecimal total,
                               List<OrderProductResponseDTO> items) {
}
