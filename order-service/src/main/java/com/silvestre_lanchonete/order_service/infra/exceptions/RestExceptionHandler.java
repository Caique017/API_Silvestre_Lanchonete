package com.silvestre_lanchonete.order_service.infra.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.stream.Collectors;

@ControllerAdvice
public class RestExceptionHandler {

    // 503 — product-service fora do ar
    @ExceptionHandler(ProductUnavailableException.class)
    public ResponseEntity<StandardError> productUnavailable(
            ProductUnavailableException e, HttpServletRequest request) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "Serviço indisponível",
                e.getMessage(), request.getRequestURI());
    }

    // 403 — sem permissão
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<StandardError> accessDenied(
            AccessDeniedException e, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "Acesso negado",
                "Você não tem permissão para acessar este recurso.",
                request.getRequestURI());
    }

    // 400 — dados inválidos
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardError> validationError(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return build(HttpStatus.BAD_REQUEST, "Dados inválidos", message, request.getRequestURI());
    }

    // 400 — outros erros de negócio
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<StandardError> runtimeError(
            RuntimeException e, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Erro na requisição",
                e.getMessage(), request.getRequestURI());
    }

    private ResponseEntity<StandardError> build(
            HttpStatus status, String error, String message, String path) {
        StandardError err = new StandardError(
                Instant.now().toEpochMilli(), status.value(), error, message, path
        );
        return ResponseEntity.status(status).body(err);
    }
}