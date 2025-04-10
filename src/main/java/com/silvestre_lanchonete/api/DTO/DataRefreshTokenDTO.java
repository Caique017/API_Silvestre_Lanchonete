package com.silvestre_lanchonete.api.DTO;

import jakarta.validation.constraints.NotBlank;

public record DataRefreshTokenDTO(@NotBlank String refreshToken) {
}
