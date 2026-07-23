package com.snowresorts.security.jwt;

import com.snowresorts.security.web.AuthenticatedUserMdcFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;

/** Shared HttpSecurity customizations for JWT resource servers. */
public final class ResourceServerHttpSecurity {

    private ResourceServerHttpSecurity() {
    }

    /**
     * Puts {@code user_id} in the MDC after JWT auth, then rejects revoked access tokens.
     */
    public static void addAccessTokenRevocationFilter(HttpSecurity http,
                                                      AccessTokenRevocationStore store) throws Exception {
        http.addFilterAfter(new AuthenticatedUserMdcFilter(), BearerTokenAuthenticationFilter.class);
        http.addFilterAfter(new AccessTokenRevocationFilter(store), AuthenticatedUserMdcFilter.class);
    }
}
