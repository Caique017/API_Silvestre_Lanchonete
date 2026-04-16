package com.silvestre_lanchonete.auth_service.dto;

public record DataEmailDTO(String email,
                           Boolean primary,
                           Boolean verified,
                           String visibility) {
}
