package com.snowresorts.security.web;

import com.snowresorts.security.logging.StructuredLogger;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * CORS allow-list that also permits the request's own host (LAN IP / simulator).
 * Never reflects arbitrary Origins and never uses {@code *}.
 *
 * <p>React Native connects to {@code ws://<lan-ip>:8080/...} and sends
 * {@code Origin: http://<lan-ip>:8080}. A static {@code localhost} allow-list alone
 * rejects that with HTTP 403 from {@code CorsFilter} before the WebSocket handshake.
 */
public final class AllowlistOrSameHostCorsConfigurationSource implements CorsConfigurationSource {

    private static final Logger log = LoggerFactory.getLogger(AllowlistOrSameHostCorsConfigurationSource.class);

    private final List<String> allowedOrigins;

    public AllowlistOrSameHostCorsConfigurationSource(List<String> allowedOrigins) {
        List<String> cleaned = new ArrayList<>();
        if (allowedOrigins != null) {
            for (String origin : allowedOrigins) {
                if (origin != null && !origin.isBlank() && !"*".equals(origin.trim())) {
                    cleaned.add(normalizeOrigin(origin.trim()));
                }
            }
        }
        this.allowedOrigins = List.copyOf(cleaned);
    }

    @Override
    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
        // Native STOMP/WebSocket: Origin is enforced by the handshake interceptor, not CorsFilter.
        // Skip by path (not only Upgrade header) — some proxies omit/alter Upgrade before CorsFilter runs.
        String path = request.getRequestURI();
        if (path != null && path.startsWith("/ws")) {
            return null;
        }
        String upgrade = request.getHeader("Upgrade");
        if (upgrade != null && "websocket".equalsIgnoreCase(upgrade.trim())) {
            return null;
        }

        String origin = request.getHeader(HttpHeaders.ORIGIN);
        if (origin == null || origin.isBlank() || "null".equalsIgnoreCase(origin)) {
            return null; // no browser CORS processing for native clients without Origin
        }

        String normalized = normalizeOrigin(origin);
        if (!isPermitted(normalized, request)) {
            StructuredLogger.of(log).warn(
                    "cors_rejected", "denied", "origin_not_allowlisted",
                    "origin", normalized,
                    "host", request.getHeader(HttpHeaders.HOST));
            // Empty allow-list → CorsFilter rejects with 403 (do not echo Origin).
            return baseConfig();
        }

        CorsConfiguration config = baseConfig();
        config.setAllowedOrigins(List.of(normalized));
        return config;
    }

    private boolean isPermitted(String origin, HttpServletRequest request) {
        for (String allowed : allowedOrigins) {
            if (allowed.equalsIgnoreCase(origin)) {
                return true;
            }
        }
        return matchesRequestHost(origin, request) || isLocalOrPrivateNetworkOrigin(origin);
    }

    /**
     * Dev / mobile-on-LAN: allow loopback and RFC1918 Origins. Public cross-site Origins
     * (e.g. https://evil.example) are still rejected unless allow-listed.
     */
    static boolean isLocalOrPrivateNetworkOrigin(String origin) {
        try {
            URI uri = URI.create(origin);
            String host = uri.getHost();
            if (host == null) {
                return false;
            }
            String h = host.toLowerCase(Locale.ROOT);
            if ("localhost".equals(h) || "127.0.0.1".equals(h) || "::1".equals(h)) {
                return true;
            }
            if (h.startsWith("10.") || h.startsWith("192.168.") || h.startsWith("169.254.")) {
                return true;
            }
            if (h.startsWith("172.")) {
                String[] parts = h.split("\\.");
                if (parts.length > 1) {
                    int second = Integer.parseInt(parts[1]);
                    return second >= 16 && second <= 31;
                }
            }
            return false;
        } catch (Exception ex) {
            return false;
        }
    }

    private static CorsConfiguration baseConfig() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedHeader(CorsConfiguration.ALL);
        config.addAllowedMethod(CorsConfiguration.ALL);
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        return config;
    }

    static boolean matchesRequestHost(String origin, HttpServletRequest request) {
        try {
            URI originUri = URI.create(origin);
            String originHost = originUri.getHost();
            if (originHost == null) {
                return false;
            }
            int originPort = effectivePort(originUri);

            String hostHeader = request.getHeader(HttpHeaders.HOST);
            if (hostHeader == null || hostHeader.isBlank()) {
                return originHost.equalsIgnoreCase(request.getServerName())
                        && originPort == request.getServerPort();
            }
            String hostOnly = hostHeader;
            int hostPort = -1;
            int colon = hostHeader.lastIndexOf(':');
            if (colon > 0 && hostHeader.indexOf(']') < colon) {
                hostOnly = hostHeader.substring(0, colon);
                hostPort = Integer.parseInt(hostHeader.substring(colon + 1));
            }
            if (hostPort < 0) {
                hostPort = request.isSecure() ? 443 : 80;
            }
            return originHost.equalsIgnoreCase(hostOnly) && originPort == hostPort;
        } catch (Exception ex) {
            return false;
        }
    }

    private static int effectivePort(URI uri) {
        int port = uri.getPort();
        if (port > 0) {
            return port;
        }
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        return ("https".equals(scheme) || "wss".equals(scheme)) ? 443 : 80;
    }

    static String normalizeOrigin(String origin) {
        String trimmed = origin.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        if (trimmed.regionMatches(true, 0, "ws://", 0, 5)) {
            trimmed = "http://" + trimmed.substring(5);
        } else if (trimmed.regionMatches(true, 0, "wss://", 0, 6)) {
            trimmed = "https://" + trimmed.substring(6);
        }
        return trimmed;
    }
}
