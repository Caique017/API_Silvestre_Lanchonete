package com.silvestre_lanchonete.api.DTO;

import org.springframework.web.multipart.MultipartFile;

public record ProductRequestDTO(String name, String description, Double price, String category, Boolean available, MultipartFile image) {
}
