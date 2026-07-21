package com.snowresorts.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RedisAccessTokenRevocationStoreTest {

    @Mock
    private StringRedisTemplate redis;
    @Mock
    private ValueOperations<String, String> values;

    @Test
    @DisplayName("isRevoked returns true when user watermark covers issuedAt")
    void isRevoked_whenWatermarkCoversIssuedAt_returnsTrue() {
        when(redis.opsForValue()).thenReturn(values);
        UUID userId = UUID.randomUUID();
        Instant issuedAt = Instant.parse("2026-07-21T12:00:00Z");
        Instant watermark = Instant.parse("2026-07-21T12:05:00Z");
        when(values.get("auth:access-revoked-before:" + userId)).thenReturn(watermark.toString());

        RedisAccessTokenRevocationStore store = new RedisAccessTokenRevocationStore(redis);

        assertThat(store.isRevoked(null, userId, issuedAt)).isTrue();
    }

    @Test
    @DisplayName("revokeJti writes a Redis key with TTL")
    void revokeJti_writesKeyWithTtl() {
        when(redis.opsForValue()).thenReturn(values);
        RedisAccessTokenRevocationStore store = new RedisAccessTokenRevocationStore(redis);

        store.revokeJti("jti-9", Duration.ofMinutes(15));

        verify(values).set(eq("auth:revoked-jti:jti-9"), eq("1"), eq(Duration.ofMinutes(15)));
    }

    @Test
    @DisplayName("isRevoked returns true when jti key exists")
    void isRevoked_whenJtiKeyExists_returnsTrue() {
        when(redis.hasKey("auth:revoked-jti:jti-9")).thenReturn(true);
        RedisAccessTokenRevocationStore store = new RedisAccessTokenRevocationStore(redis);

        assertThat(store.isRevoked("jti-9", UUID.randomUUID(), Instant.now())).isTrue();
    }
}
