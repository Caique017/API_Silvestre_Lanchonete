package com.silvestre_lanchonete.auth_service.infra.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    // 404 - Not Found (Usuário não encontrado)
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<StandardError> userNotFound(UserNotFoundException e, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Não encontrado", e.getMessage(), request.getRequestURI());
    }

    // 401 - Unauthorized (Senha incorreta)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<StandardError> badCredentials(BadCredentialsException e, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "Credenciais inválidas",
                "E-mail ou senha incorretos.",
                request.getRequestURI()
        );
    }

    // 401 - Unauthorized (Token inválido ou expirado)
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<StandardError> invalidToken(InvalidTokenException e, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Token inválido", e.getMessage(), request.getRequestURI());
    }

    // 403 - FORBIDDEN (Usuário logado, mas não tem a permissão necessária/Role)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<StandardError> accessDenied(AccessDeniedException e, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "Acesso negado", "Você não tem permissão para acessar este recurso", request.getRequestURI());
    }

    // 409 - Conflict (E-mail já cadastrado)
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<StandardError> userAlreadyExists(UserAlreadyExistsException e, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "Conflito de dados", e.getMessage(), request.getRequestURI());
    }

    // 400 - BAD REQUEST (Erros de sintaxe ou regras de negócio violadas propositalmente)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<StandardError> badRequest(IllegalArgumentException e, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Requisição inválida", e.getMessage(), request.getRequestURI());
    }

    // 400 - VALIDATION ERROR (Captura os erros do @Valid nos DTOs)
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        StandardError err = new StandardError(
                System.currentTimeMillis(),
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validação nos campos",
                message,
                ((ServletWebRequest)request).getRequest().getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    // 500 - INTERNAL SERVER ERROR (O "catch-all" para erros inesperados)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardError> internalError(Exception e, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", "Ocorreu um erro inesperado no servidor: " + e.getMessage(), request.getRequestURI());
    }

    private ResponseEntity<StandardError> buildResponse(HttpStatus status, String error, String message, String path) {
        StandardError err = new StandardError(System.currentTimeMillis(), status.value(), error, message, path);
        return ResponseEntity.status(status).body(err);
    }
}