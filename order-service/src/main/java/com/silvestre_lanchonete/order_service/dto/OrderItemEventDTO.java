package com.silvestre_lanchonete.order_service.dto;

import java.util.UUID;

public record OrderItemEventDTO(UUID productId,
                                Integer amount) {
}
