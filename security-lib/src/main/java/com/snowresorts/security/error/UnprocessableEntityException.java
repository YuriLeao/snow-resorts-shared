package com.snowresorts.security.error;

import org.springframework.http.HttpStatus;

/** Thrown when input is well-formed but semantically invalid for a business rule. Renders HTTP 422. */
public class UnprocessableEntityException extends ApiException {

    public UnprocessableEntityException(String detail) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", detail);
    }
}
