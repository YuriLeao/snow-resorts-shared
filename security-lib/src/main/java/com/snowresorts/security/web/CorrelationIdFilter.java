package com.snowresorts.security.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Propagates an {@code X-Trace-Id} correlation id from the edge (ALB / mobile) into the
 * SLF4J {@link MDC} and the response, generating one when absent.
 *
 * <p>MDC keys: {@code request_id} (canonical) and {@code traceId} (same value, for compat).
 * Order is set on the {@code FilterRegistrationBean} in {@code SecurityLibAutoConfiguration}.
 */
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String MDC_REQUEST_ID = "request_id";
    /** Alias of {@link #MDC_REQUEST_ID} kept for existing docs / Insights queries. */
    public static final String MDC_TRACE_ID = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestId = request.getHeader(TRACE_ID_HEADER);
        if (!StringUtils.hasText(requestId)) {
            requestId = UUID.randomUUID().toString();
        }
        MDC.put(MDC_REQUEST_ID, requestId);
        MDC.put(MDC_TRACE_ID, requestId);
        response.setHeader(TRACE_ID_HEADER, requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_REQUEST_ID);
            MDC.remove(MDC_TRACE_ID);
            MDC.remove(AuthenticatedUserMdcFilter.MDC_USER_ID);
        }
    }
}
