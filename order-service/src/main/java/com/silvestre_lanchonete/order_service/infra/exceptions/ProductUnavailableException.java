package com.silvestre_lanchonete.order_service.infra.exceptions;

public class ProductUnavailableException extends RuntimeException {
    public ProductUnavailableException(String message) {
        super(message);
    }
}
