package com.silvestre_lanchonete.auth_service.dto;

public record PasswordResetTokenDTO(String token, String newPassword) {
}
