package com.snowresorts.security;

import com.snowresorts.security.error.GlobalExceptionHandler;
import com.snowresorts.security.jwt.ResourceServerProperties;
import com.snowresorts.security.jwt.ResourceServerSecurityConfig;
import com.snowresorts.security.web.CorrelationIdFilter;
import com.snowresorts.security.web.SecurityHeadersFilter;
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
@AutoConfiguration
@EnableConfigurationProperties(ResourceServerProperties.class)
@Import(ResourceServerSecurityConfig.class)
public class SecurityLibAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    GlobalExceptionHandler snowGlobalExceptionHandler() {
        return new GlobalExceptionHandler();
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
