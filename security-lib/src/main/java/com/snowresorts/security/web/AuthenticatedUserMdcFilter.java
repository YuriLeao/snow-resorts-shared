package com.snowresorts.security.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * After JWT authentication, puts {@code user_id} (JWT {@code sub}) into the MDC so every
 * subsequent structured log line is filterable by user.
 */
public class AuthenticatedUserMdcFilter extends OncePerRequestFilter {

    public static final String MDC_USER_ID = "user_id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            String sub = jwt.getSubject();
            if (sub != null && !sub.isBlank()) {
                try {
                    UUID.fromString(sub);
                    MDC.put(MDC_USER_ID, sub);
                } catch (IllegalArgumentException ignored) {
                    // ignore malformed sub
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
