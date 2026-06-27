package com.snowresorts.security.jwt;

import java.util.Collection;
import java.util.List;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Maps a validated {@link Jwt} into a {@link JwtAuthenticationToken}, deriving authorities
 * from a configurable claim (default {@code roles}) and applying an authority prefix.
 */
public class JwtAuthoritiesConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final String authoritiesClaim;
    private final String authorityPrefix;

    public JwtAuthoritiesConverter(String authoritiesClaim, String authorityPrefix) {
        this.authoritiesClaim = authoritiesClaim;
        this.authorityPrefix = authorityPrefix;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractRoles(jwt).stream()
                .map(role -> new SimpleGrantedAuthority(authorityPrefix + role))
                .map(GrantedAuthority.class::cast)
                .toList();
        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }

    @SuppressWarnings("unchecked")
    private List<String> extractRoles(Jwt jwt) {
        Object claim = jwt.getClaim(authoritiesClaim);
        if (claim instanceof Collection<?> collection) {
            return collection.stream().map(String::valueOf).toList();
        }
        if (claim instanceof String s && !s.isBlank()) {
            return List.of(s.split("\\s+"));
        }
        return List.of();
    }
}
