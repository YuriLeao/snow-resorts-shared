package com.snowresorts.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Rejects authenticated requests whose access token was revoked after logout / password reset.
 */
public class AccessTokenRevocationFilter extends OncePerRequestFilter {

    private final AccessTokenRevocationStore store;

    public AccessTokenRevocationFilter(AccessTokenRevocationStore store) {
        this.store = store;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            UUID userId = null;
            try {
                if (jwt.getSubject() != null) {
                    userId = UUID.fromString(jwt.getSubject());
                }
            } catch (IllegalArgumentException ignored) {
                // malformed sub — leave userId null; jti check may still apply
            }
            if (store.isRevoked(jwt.getId(), userId, jwt.getIssuedAt())) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                response.getWriter().write(
                        "{\"title\":\"Unauthorized\",\"status\":401,\"detail\":\"Access token has been revoked.\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
