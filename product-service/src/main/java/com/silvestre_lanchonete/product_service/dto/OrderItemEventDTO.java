package com.silvestre_lanchonete.product_service.dto;

import java.util.UUID;

public record OrderItemEventDTO(
        UUID productId,
        Integer amount
) {}