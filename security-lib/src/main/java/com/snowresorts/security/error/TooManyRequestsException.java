package com.snowresorts.security.error;

import org.springframework.http.HttpStatus;

/** Maps to HTTP 429 when a client exceeds a rate or quota limit. */
public class TooManyRequestsException extends ApiException {

    public TooManyRequestsException(String detail) {
        super(HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", detail);
    }
}
