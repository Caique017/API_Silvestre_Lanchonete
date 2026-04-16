package com.silvestre_lanchonete.order_service.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderProductResponseDTO(UUID productId,
                                      Integer amount,
                                      BigDecimal price) {
}
