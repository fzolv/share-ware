package com.fzolv.shareware.hull.configs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage(), ex);
        return error(HttpStatus.BAD_REQUEST, 4001, "Validation failed");
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, Object>> handleBindException(BindException ex) {
        log.error("Bind error: {}", ex.getMessage(), ex);
        return error(HttpStatus.BAD_REQUEST, 4002, "Invalid request parameters");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        log.error("Constraint violation: {}", ex.getMessage(), ex);
        return error(HttpStatus.BAD_REQUEST, 4003, "Constraint violation");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleNotReadable(HttpMessageNotReadableException ex) {
        log.error("Message not readable: {}", ex.getMessage(), ex);
        return error(HttpStatus.BAD_REQUEST, 4004, "Malformed JSON request");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(MissingServletRequestParameterException ex) {
        log.error("Missing request parameter: {}", ex.getMessage(), ex);
        return error(HttpStatus.BAD_REQUEST, 4005, "Missing request parameter");
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<Map<String, Object>> handleMissingPath(MissingPathVariableException ex) {
        log.error("Missing path variable: {}", ex.getMessage(), ex);
        return error(HttpStatus.BAD_REQUEST, 4006, "Missing path variable");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.error("Method not supported: {}", ex.getMessage(), ex);
        return error(HttpStatus.METHOD_NOT_ALLOWED, 4051, "HTTP method not supported");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        log.error("Media type not supported: {}", ex.getMessage(), ex);
        return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, 4151, "Media type not supported");
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<Map<String, Object>> handleMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex) {
        log.error("Media type not acceptable: {}", ex.getMessage(), ex);
        return error(HttpStatus.NOT_ACCEPTABLE, 4061, "Media type not acceptable");
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoHandler(NoHandlerFoundException ex) {
        log.error("No handler found: {}", ex.getMessage(), ex);
        return error(HttpStatus.NOT_FOUND, 4041, "Resource not found");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.error("Data integrity violation: {}", ex.getMessage(), ex);
        return error(HttpStatus.CONFLICT, 4091, "Data integrity violation");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        log.error("Access denied: {}", ex.getMessage(), ex);
        return error(HttpStatus.FORBIDDEN, 4031, "Access denied");
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAuthzDenied(AuthorizationDeniedException ex) {
        log.error("Authentication error: {}", ex.getMessage(), ex);
        return error(HttpStatus.UNAUTHORIZED, 4012, "Failed to authorize");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuth(AuthenticationException ex) {
        log.error("Authentication error: {}", ex.getMessage(), ex);
        return error(HttpStatus.UNAUTHORIZED, 4011, "Failed to authenticate");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Illegal argument: {}", ex.getMessage(), ex);
        return error(HttpStatus.BAD_REQUEST, 4007, "Invalid argument");
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        log.error("Illegal state: {}", ex.getMessage(), ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, 5002, "Illegal state");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;
        log.error("ResponseStatus exception: {}", ex.getMessage(), ex);
        int errorCode = status.value() * 10 + 1; // derive incremental code from status
        return error(status, errorCode, ex.getReason() != null ? ex.getReason() : "Request failed");
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Throwable ex) {
        log.error("Unhandled error: {}", ex.getMessage(), ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, 5001, "Internal Server Error");
    }

    private static ResponseEntity<Map<String, Object>> error(HttpStatus status, int errorCode, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("errorCode", errorCode);
        body.put("message", message);
        body.put("statusCode", status.value());
        return ResponseEntity.status(status).body(body);
    }
}


