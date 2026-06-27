package com.snowresorts.security.jwt;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Typed configuration for the shared resource-server security setup.
 *
 * <pre>
 * snow:
 *   security:
 *     enabled: true
 *     public-paths:
 *       - /actuator/health/**
 *     authorities-claim: roles
 * </pre>
 */
@ConfigurationProperties(prefix = "snow.security")
public class ResourceServerProperties {

    /** Master switch for the shared resource-server {@code SecurityFilterChain}. */
    private boolean enabled = true;

    /** Additional endpoints (beyond the always-public defaults) that bypass authentication. */
    private List<String> publicPaths = new ArrayList<>();

    /** JWT claim that carries the caller's roles/authorities. */
    private String authoritiesClaim = "roles";

    /** Prefix applied to mapped authorities (e.g. {@code ROLE_}). */
    private String authorityPrefix = "ROLE_";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getPublicPaths() {
        return publicPaths;
    }

    public void setPublicPaths(List<String> publicPaths) {
        this.publicPaths = publicPaths;
    }

    public String getAuthoritiesClaim() {
        return authoritiesClaim;
    }

    public void setAuthoritiesClaim(String authoritiesClaim) {
        this.authoritiesClaim = authoritiesClaim;
    }

    public String getAuthorityPrefix() {
        return authorityPrefix;
    }

    public void setAuthorityPrefix(String authorityPrefix) {
        this.authorityPrefix = authorityPrefix;
    }
}
