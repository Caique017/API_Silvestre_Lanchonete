package com.silvestre_lanchonete.product_service.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEventDTO(
        UUID orderId,
        String userEmail,
        BigDecimal total,
        List<OrderItemEventDTO> items
) {}