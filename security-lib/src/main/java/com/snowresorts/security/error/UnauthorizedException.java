package com.snowresorts.security.error;

import org.springframework.http.HttpStatus;

/** Thrown on failed authentication (bad credentials, invalid/expired token). Renders HTTP 401. */
public class UnauthorizedException extends ApiException {

    public UnauthorizedException(String detail) {
        super(HttpStatus.UNAUTHORIZED, "Unauthorized", detail);
    }
}
