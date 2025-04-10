package com.silvestre_lanchonete.api.DTO;

public record DataEmailDTO(String email,
                           Boolean primary,
                           Boolean verified,
                           String visibility) {
}
