package com.snowresorts.security.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsConfiguration;

@DisplayName("AllowlistOrSameHostCorsConfigurationSource")
class AllowlistOrSameHostCorsConfigurationSourceTest {

    @Test
    void missingOrigin_skipsCorsProcessing() {
        AllowlistOrSameHostCorsConfigurationSource source =
                new AllowlistOrSameHostCorsConfigurationSource(List.of("http://localhost:8080"));
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HttpHeaders.ORIGIN)).thenReturn(null);

        assertThat(source.getCorsConfiguration(request)).isNull();
    }

    @Test
    void allowlistedOrigin_isAccepted() {
        AllowlistOrSameHostCorsConfigurationSource source =
                new AllowlistOrSameHostCorsConfigurationSource(List.of("http://localhost:8080"));
        HttpServletRequest request = request("http://localhost:8080", "192.168.3.18:8080");

        CorsConfiguration config = source.getCorsConfiguration(request);
        assertThat(config).isNotNull();
        assertThat(config.getAllowedOrigins()).containsExactly("http://localhost:8080");
    }

    @Test
    void sameHostLanOrigin_isAccepted() {
        AllowlistOrSameHostCorsConfigurationSource source =
                new AllowlistOrSameHostCorsConfigurationSource(List.of("http://localhost:8080"));
        HttpServletRequest request = request("http://192.168.3.18:8080", "192.168.3.18:8080");

        CorsConfiguration config = source.getCorsConfiguration(request);
        assertThat(config).isNotNull();
        assertThat(config.getAllowedOrigins()).containsExactly("http://192.168.3.18:8080");
    }

    @Test
    void websocketUpgrade_skipsCorsFilter() {
        AllowlistOrSameHostCorsConfigurationSource source =
                new AllowlistOrSameHostCorsConfigurationSource(List.of("http://localhost:8080"));
        HttpServletRequest request = request("http://192.168.3.18:8080", "192.168.3.18:8080");
        when(request.getHeader("Upgrade")).thenReturn("websocket");

        assertThat(source.getCorsConfiguration(request)).isNull();
    }

    @Test
    void crossSiteOrigin_isRejected() {
        AllowlistOrSameHostCorsConfigurationSource source =
                new AllowlistOrSameHostCorsConfigurationSource(List.of("http://localhost:8080"));
        HttpServletRequest request = request("https://evil.example", "192.168.3.18:8080");

        CorsConfiguration config = source.getCorsConfiguration(request);
        assertThat(config).isNotNull();
        assertThat(config.getAllowedOrigins()).isNullOrEmpty();
    }

    private static HttpServletRequest request(String origin, String host) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HttpHeaders.ORIGIN)).thenReturn(origin);
        when(request.getHeader(HttpHeaders.HOST)).thenReturn(host);
        return request;
    }
}
