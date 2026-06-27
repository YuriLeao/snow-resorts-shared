package com.snowresorts.security.web;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class SecurityHeadersFilterTest {

    private final SecurityHeadersFilter filter = new SecurityHeadersFilter();

    @Test
    @DisplayName("doFilterInternal sets the OWASP baseline security headers")
    void doFilterInternal_setsOwaspHeaders() throws Exception {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/snow-resort-service/v1/resorts");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = new MockFilterChain();

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        assertThat(response.getHeader("X-Content-Type-Options")).isEqualTo("nosniff");
        assertThat(response.getHeader("X-Frame-Options")).isEqualTo("DENY");
        assertThat(response.getHeader("Content-Security-Policy")).contains("frame-ancestors 'none'");
        assertThat(response.getHeader("Strict-Transport-Security")).contains("max-age=");
    }

    /** Minimal terminal filter chain that records invocation without a servlet container. */
    private static final class MockFilterChain implements FilterChain {
        @Override
        public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response) {
            // no-op terminal
        }
    }
}
