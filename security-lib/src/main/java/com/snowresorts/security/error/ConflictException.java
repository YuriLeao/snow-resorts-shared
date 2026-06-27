package com.snowresorts.security.error;

import org.springframework.http.HttpStatus;

/** Thrown when a request conflicts with the current state of a resource. Renders HTTP 409. */
public class ConflictException extends ApiException {

    public ConflictException(String detail) {
        super(HttpStatus.CONFLICT, "Conflict", detail);
    }
}
