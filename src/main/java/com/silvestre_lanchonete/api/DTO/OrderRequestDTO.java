package com.silvestre_lanchonete.api.DTO;

import java.util.List;
import java.util.UUID;

public record OrderRequestDTO(UUID userId, List<OrderProductRequest> products) {
    public static record OrderProductRequest(UUID productId, int quantity) {}
}
