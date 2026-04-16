package com.silvestre_lanchonete.product_service.dto;

import org.springframework.web.multipart.MultipartFile;

public record ProductRequestDTO(String name, String description, Double price, String category, Boolean available, MultipartFile image) {
}
