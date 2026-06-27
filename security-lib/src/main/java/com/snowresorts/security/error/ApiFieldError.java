package com.snowresorts.security.error;

/** A single field-level validation error included under the {@code errors} property of a Problem Detail. */
public record ApiFieldError(String field, String message, Object rejectedValue) {
}
