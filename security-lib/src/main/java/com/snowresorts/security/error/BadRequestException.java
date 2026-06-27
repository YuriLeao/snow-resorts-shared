package com.snowresorts.security.error;

import org.springframework.http.HttpStatus;

/** Thrown for malformed or semantically invalid input that is not a bean-validation failure. Renders HTTP 400. */
public class BadRequestException extends ApiException {

    public BadRequestException(String detail) {
        super(HttpStatus.BAD_REQUEST, "Bad Request", detail);
    }
}
