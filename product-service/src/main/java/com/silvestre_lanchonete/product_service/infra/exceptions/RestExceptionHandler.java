package com.silvestre_lanchonete.product_service.infra.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler {

    // 404 — produto não encontrado
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<StandardError> productNotFound(ProductNotFoundException e, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Não encontrado", e.getMessage(), request.getRequestURI());
    }

    // 403 — sem permissão de role
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<StandardError> accessDenied(AccessDeniedException e, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "Acesso negado", "Você não tem permissão para acessar este recurso.", request.getRequestURI());
    }

    // 400 — argumento inválido
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<StandardError> badRequest(IllegalArgumentException e, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Requisição inválida", e.getMessage(), request.getRequestURI());
    }

    // 500 — catch-all para erros inesperados
    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardError> internalError(Exception e, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", "Ocorreu um erro inesperado: " + e.getMessage(), request.getRequestURI());
    }

    private ResponseEntity<StandardError> buildResponse(HttpStatus status, String error, String message, String path) {
        StandardError err = new StandardError(System.currentTimeMillis(), status.value(), error, message, path);
        return ResponseEntity.status(status).body(err);
    }
}