package com.snowresorts.security.jwt;

import com.snowresorts.security.SecurityLibAutoConfiguration;
import com.snowresorts.security.logging.StructuredLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Prefers Redis for access-token revocation when a {@link StringRedisTemplate} is available.
 *
 * <p>Must run after {@link RedisAutoConfiguration} (so the template exists) and before
 * {@link SecurityLibAutoConfiguration} (so the in-memory fallback is skipped). Without this
 * ordering, each JVM keeps a private denylist and logout only affects the auth-service process.
 */
@AutoConfiguration(
        after = RedisAutoConfiguration.class,
        before = SecurityLibAutoConfiguration.class)
@ConditionalOnClass(StringRedisTemplate.class)
public class RedisAccessTokenRevocationAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RedisAccessTokenRevocationAutoConfiguration.class);

    @Bean
    @ConditionalOnBean(StringRedisTemplate.class)
    @ConditionalOnMissingBean(AccessTokenRevocationStore.class)
    AccessTokenRevocationStore redisAccessTokenRevocationStore(StringRedisTemplate redis) {
        StructuredLogger.of(log).info("revocation_store", "accepted", "redis_backed");
        return new RedisAccessTokenRevocationStore(redis);
    }
}
