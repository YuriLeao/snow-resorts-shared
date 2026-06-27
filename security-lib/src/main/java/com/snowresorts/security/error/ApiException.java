package com.snowresorts.security.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/**
 * Base class for domain/application exceptions that map directly to an HTTP status and an
 * RFC 7807 {@code ProblemDetail}. Services should extend the specialised subclasses
 * (e.g. {@link ResourceNotFoundException}) instead of throwing raw exceptions so the
 * shared {@link GlobalExceptionHandler} can render a consistent error contract.
 */
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String title;

    protected ApiException(HttpStatus status, String title, String detail) {
        super(detail);
        this.status = status;
        this.title = title;
    }

    public HttpStatusCode getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }
}
