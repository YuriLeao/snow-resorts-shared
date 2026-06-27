package com.snowresorts.security.error;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an authenticated caller is not allowed to access a resource (e.g. IDOR
 * protection: viewing a non-friend's private stats). Renders HTTP 403.
 */
public class ForbiddenException extends ApiException {

    public ForbiddenException(String detail) {
        super(HttpStatus.FORBIDDEN, "Forbidden", detail);
    }
}
