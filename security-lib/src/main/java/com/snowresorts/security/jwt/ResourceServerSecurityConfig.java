package com.snowresorts.security.jwt;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Default stateless JWT resource-server security for downstream services (user, resort,
 * location, activity). Services that need a bespoke chain (e.g. {@code auth-service})
 * simply declare their own {@link SecurityFilterChain} bean, which disables this default
 * via {@link ConditionalOnMissingBean}.
 *
 * <p>Token validation relies on Spring Boot's auto-configured {@code JwtDecoder}, driven by
 * {@code spring.security.oauth2.resourceserver.jwt.jwk-set-uri} pointing at the
 * auth-service JWKS endpoint.
 */
@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@ConditionalOnProperty(prefix = "snow.security", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ResourceServerSecurityConfig {

    private static final String[] ALWAYS_PUBLIC = {
            "/actuator/health/**",
            "/actuator/health",
            "/actuator/info",
            "/actuator/prometheus",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    private final ResourceServerProperties properties;

    public ResourceServerSecurityConfig(ResourceServerProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    SecurityFilterChain resourceServerFilterChain(HttpSecurity http) throws Exception {
        List<String> publicPaths = new ArrayList<>(List.of(ALWAYS_PUBLIC));
        publicPaths.addAll(properties.getPublicPaths());

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(publicPaths.toArray(String[]::new)).permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(
                        new JwtAuthoritiesConverter(properties.getAuthoritiesClaim(),
                                properties.getAuthorityPrefix()))));

        return http.build();
    }
}
