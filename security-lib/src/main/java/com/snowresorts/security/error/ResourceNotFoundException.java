package com.snowresorts.security.error;

import org.springframework.http.HttpStatus;

/** Thrown when a requested resource does not exist. Renders HTTP 404. */
public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String detail) {
        super(HttpStatus.NOT_FOUND, "Resource Not Found", detail);
    }

    public static ResourceNotFoundException of(String resource, Object id) {
        return new ResourceNotFoundException("%s with id '%s' was not found.".formatted(resource, id));
    }
}
