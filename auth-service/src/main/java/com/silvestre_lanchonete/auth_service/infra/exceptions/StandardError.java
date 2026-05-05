package com.silvestre_lanchonete.auth_service.infra.exceptions;

public record StandardError(
        Long timestamp,
        Integer status,
        String error,
        String message,
        String path
) {}
