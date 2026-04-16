package com.silvestre_lanchonete.auth_service.dto;

import jakarta.validation.constraints.NotBlank;

public record DataRefreshTokenDTO(@NotBlank String refreshToken) {
}
