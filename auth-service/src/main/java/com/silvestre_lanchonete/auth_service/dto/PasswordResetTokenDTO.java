package com.silvestre_lanchonete.auth_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetTokenDTO(String token,
                                    @NotBlank(message = "A senha é obrigatória")
                                    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres") String newPassword) {
}
