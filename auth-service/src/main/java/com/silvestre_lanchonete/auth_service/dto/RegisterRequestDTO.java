package com.silvestre_lanchonete.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO (@NotBlank(message = "O nome é obrigatório")
                                  String name,

                                  @NotBlank
                                  @Email
                                  @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                                          message = "Formato de e-mail inválido. Ex: usuario@dominio.com")
                                  String email,
                                  @NotBlank(message = "A senha é obrigatória")
                                  @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
                                  String password) {
}