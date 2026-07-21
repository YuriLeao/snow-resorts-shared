package com.snowresorts.security.jwt;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;

/** Shared HttpSecurity customizations for JWT resource servers. */
public final class ResourceServerHttpSecurity {

    private ResourceServerHttpSecurity() {
    }

    /** Rejects JWTs that were revoked after logout / password reset. */
    public static void addAccessTokenRevocationFilter(HttpSecurity http,
                                                      AccessTokenRevocationStore store) throws Exception {
        http.addFilterAfter(new AccessTokenRevocationFilter(store), BearerTokenAuthenticationFilter.class);
    }
}
