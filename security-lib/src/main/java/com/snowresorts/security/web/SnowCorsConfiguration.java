package com.snowresorts.security.web;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Registers an allow-list {@link CorsConfigurationSource} that also permits same-host
 * Origins (LAN IP / simulator). Never uses {@code *} and never echoes untrusted Origins.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(CorsProperties.class)
public class SnowCorsConfiguration {

    @Bean
    @ConditionalOnMissingBean(CorsConfigurationSource.class)
    CorsConfigurationSource corsConfigurationSource(CorsProperties properties) {
        return new AllowlistOrSameHostCorsConfigurationSource(properties.getAllowedOrigins());
    }
}
