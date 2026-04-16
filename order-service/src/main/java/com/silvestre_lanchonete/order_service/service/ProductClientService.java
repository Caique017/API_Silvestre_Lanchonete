package com.silvestre_lanchonete.order_service.service;

import com.silvestre_lanchonete.order_service.dto.ProductResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Service
public class ProductClientService {

    private final RestClient restClient;

    public ProductClientService(@Value("${product.service.url}") String productServiceUrl) {
        this.restClient = RestClient.create(productServiceUrl);
    }

    public ProductResponseDTO getProductById(UUID productId, String token) {
        return restClient.get()
                .uri("/products/{id}", productId)
                .header("Authorization", token)
                .retrieve()
                .body(ProductResponseDTO.class);
    }
}