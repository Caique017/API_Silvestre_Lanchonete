package com.silvestre_lanchonete.product_service.infra.exceptions;

public record StandardError(
        long timestamp,
        int status,
        String error,
        String message,
        String path
) {}
