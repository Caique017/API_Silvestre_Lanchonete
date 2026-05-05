package com.silvestre_lanchonete.order_service.service;

import com.silvestre_lanchonete.order_service.dto.ProductResponseDTO;
import com.silvestre_lanchonete.order_service.infra.config.AuthTokenInterceptor;
import com.silvestre_lanchonete.order_service.infra.exceptions.ProductUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Service
public class ProductClientService {

    private static final Logger log = LoggerFactory.getLogger(ProductClientService.class);

    private final RestClient restClient;

    public ProductClientService(
            @Value("${product.service.url}") String productServiceUrl,
            AuthTokenInterceptor authTokenInterceptor) {

        this.restClient = RestClient.builder()
                .baseUrl(productServiceUrl)
                .requestInterceptor(authTokenInterceptor)
                .build();
    }

    public ProductResponseDTO getProductById(UUID productId) {
        try {
            return restClient.get()
                    .uri("/products/{id}", productId)
                    .retrieve()
                    .body(ProductResponseDTO.class);

        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException("Produto " + productId + " não encontrado.");

        } catch (Exception e) {
            log.error("[ProductClient] Product-service indisponível para produto {}: {}",
                    productId, e.getMessage());

            throw new ProductUnavailableException(
                    "Serviço de produtos temporariamente indisponível. " +
                            "Tente novamente em alguns instantes."
            );
        }
    }
}