package com.eventsaathi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a business rule is violated (e.g., duplicate booking,
 * vendor already approved, etc.). Maps to HTTP 409 Conflict.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
