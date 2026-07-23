package com.snowresorts.security;

import com.snowresorts.security.error.GlobalExceptionHandler;
import com.snowresorts.security.jwt.AccessTokenRevocationStore;
import com.snowresorts.security.jwt.InMemoryAccessTokenRevocationStore;
import com.snowresorts.security.jwt.RedisAccessTokenRevocationAutoConfiguration;
import com.snowresorts.security.jwt.ResourceServerProperties;
import com.snowresorts.security.jwt.ResourceServerSecurityConfig;
import com.snowresorts.security.logging.StructuredLogger;
import com.snowresorts.security.web.CorrelationIdFilter;
import com.snowresorts.security.web.SecurityHeadersFilter;
import com.snowresorts.security.web.SnowCorsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

/**
 * Auto-configuration entry point for {@code security-lib}. Adding the dependency wires in
 * the RFC 7807 {@link GlobalExceptionHandler} and the default stateless JWT resource-server
 * {@code SecurityFilterChain} (see {@link ResourceServerSecurityConfig}) with zero boilerplate
 * in each service.
 */
@AutoConfiguration(after = RedisAccessTokenRevocationAutoConfiguration.class)
@EnableConfigurationProperties(ResourceServerProperties.class)
@Import({ResourceServerSecurityConfig.class, SnowCorsConfiguration.class})
public class SecurityLibAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SecurityLibAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    GlobalExceptionHandler snowGlobalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    /**
     * Process-local fallback when Redis is not on the classpath / not configured.
     * Multi-service logout denylist requires the Redis-backed store.
     */
    @Bean
    @ConditionalOnMissingBean(AccessTokenRevocationStore.class)
    AccessTokenRevocationStore inMemoryAccessTokenRevocationStore() {
        StructuredLogger.of(log).warn("revocation_store", "accepted", "in_memory_fallback");
        return new InMemoryAccessTokenRevocationStore();
    }

    @Bean
    FilterRegistrationBean<CorrelationIdFilter> correlationIdFilterRegistration() {
        FilterRegistrationBean<CorrelationIdFilter> registration =
                new FilterRegistrationBean<>(new CorrelationIdFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilterRegistration() {
        FilterRegistrationBean<SecurityHeadersFilter> registration =
                new FilterRegistrationBean<>(new SecurityHeadersFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        registration.addUrlPatterns("/*");
        return registration;
    }
}
