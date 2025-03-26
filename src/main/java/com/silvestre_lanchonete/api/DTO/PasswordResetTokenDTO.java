package com.silvestre_lanchonete.api.DTO;

public record PasswordResetTokenDTO(String token, String newPassword) {
}
