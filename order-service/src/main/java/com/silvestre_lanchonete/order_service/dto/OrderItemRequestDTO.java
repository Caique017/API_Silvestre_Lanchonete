package com.silvestre_lanchonete.order_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record OrderItemRequestDTO(@NotNull(message = "O ID do produto é obrigatório")
                                  UUID productId,

                                  @NotNull(message = "A quantidade é obrigatória")
                                  @Min(value = 1, message = "A quantidade mínima é 1")
                                  Integer amount) {
}
