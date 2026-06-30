package com.snowresorts.security;

import com.snowresorts.security.error.ForbiddenException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/** Convenience accessors for the authenticated principal (the JWT {@code sub} = user id). */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /** @return the authenticated user id ({@code sub}), or empty when unauthenticated. */
    private static Optional<UUID> currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken token) {
            return Optional.ofNullable(token.getToken().getSubject()).map(UUID::fromString);
        }
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return Optional.ofNullable(jwt.getSubject()).map(UUID::fromString);
        }
        return Optional.empty();
    }

    /** @return the authenticated user id, or throws {@link ForbiddenException} when absent. */
    public static UUID requireCurrentUserId() {
        return currentUserId().orElseThrow(() -> new ForbiddenException("Authentication is required."));
    }
}
