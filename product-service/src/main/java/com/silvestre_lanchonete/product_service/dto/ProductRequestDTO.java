package com.silvestre_lanchonete.product_service.dto;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

public record ProductRequestDTO(

        @NotBlank(message = "O nome do produto é obrigatório")
        @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
        String name,

        @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
        String description,

        @NotNull(message = "O preço é obrigatório")
        @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
        @DecimalMax(value = "99999.99", message = "O preço não pode ultrapassar R$ 99.999,99")
        Double price,

        @NotBlank(message = "A categoria é obrigatória")
        @Size(max = 50, message = "A categoria deve ter no máximo 50 caracteres")
        String category,

        @NotNull(message = "A disponibilidade é obrigatória")
        Boolean available,

        MultipartFile image
) {}