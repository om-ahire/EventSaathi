package com.eventsaathi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested resource is not found (maps to HTTP 404).
 *
 * WHY extend RuntimeException?
 *   Checked exceptions force callers to handle or declare them, cluttering
 *   service layer signatures. Spring's @ExceptionHandler picks up unchecked
 *   exceptions cleanly without propagation boilerplate.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    // Convenience factory: ResourceNotFoundException.of("Vendor", "id", 42)
    public static ResourceNotFoundException of(String resource, String field, Object value) {
        return new ResourceNotFoundException(
                String.format("%s not found with %s: '%s'", resource, field, value)
        );
    }
}
