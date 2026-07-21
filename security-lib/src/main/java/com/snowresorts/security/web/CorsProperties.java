package com.snowresorts.security.web;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Explicit browser CORS allow-list. Empty (default) means no cross-origin browser access.
 *
 * <pre>
 * snow:
 *   security:
 *     cors:
 *       allowed-origins:
 *         - http://localhost:8080
 * </pre>
 */
@ConfigurationProperties(prefix = "snow.security.cors")
public class CorsProperties {

    private List<String> allowedOrigins = new ArrayList<>();

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins != null ? allowedOrigins : new ArrayList<>();
    }
}
