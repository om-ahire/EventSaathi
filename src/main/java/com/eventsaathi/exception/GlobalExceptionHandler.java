package com.eventsaathi.exception;

import com.eventsaathi.dto.ApiDtos.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * GlobalExceptionHandler – single entry-point for all unhandled exceptions.
 *
 * WHY @RestControllerAdvice?
 *   @ControllerAdvice + @ResponseBody combined. Every @ExceptionHandler here
 *   applies to ALL controllers, so we never repeat try-catch blocks in
 *   controllers. This is the standard production pattern.
 *
 * ERROR JSON CONTRACT (all phases must honour this shape):
 * {
 *   "timestamp": "2024-01-01T10:00:00",
 *   "status":    500,
 *   "error":     "Internal Server Error",
 *   "message":   "...",
 *   "path":      "/api/v1/..."
 * }
 *
 * We'll add more @ExceptionHandler methods in Phase 2, 3, 4, etc.
 * The shape must NEVER change – client code depends on it.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(
            BusinessException ex, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {
        return buildError(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::formatFieldError)
                .collect(Collectors.joining("; "));
        return buildError(HttpStatus.BAD_REQUEST, "Bad Request", message, request.getRequestURI());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "Bad Request",
                "Request body is missing or contains invalid data", request.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, "Conflict",
                "The request conflicts with existing data", request.getRequestURI());
    }

    private static String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    // ── Catch-all – must always be the last resort ───────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unhandled exception at [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);

        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // ── Shared builder used by all handlers ──────────────────────────────────
    public static ResponseEntity<ErrorResponse> buildError(
            HttpStatus status,
            String error,
            String message,
            String path) {

        return new ResponseEntity<>(
                new ErrorResponse(LocalDateTime.now(), status.value(), error, message, path), status);
    }
}
