package com.silvestre_lanchonete.api.DTO;

import java.util.List;
import java.util.UUID;

public record OrderResponseDTO(
        UUID id,
        String status,
        Double total,
        List<OrderProductResponse> products
) {
    public record OrderProductResponse(UUID productId, String name, int amount, double price) {}
}
