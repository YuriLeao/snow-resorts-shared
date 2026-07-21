package com.snowresorts.security.jwt;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;

/** Redis-backed revocation store shared across service instances. */
public class RedisAccessTokenRevocationStore implements AccessTokenRevocationStore {

    private static final String JTI_KEY_PREFIX = "auth:revoked-jti:";
    private static final String USER_KEY_PREFIX = "auth:access-revoked-before:";

    private final StringRedisTemplate redis;

    public RedisAccessTokenRevocationStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void revokeJti(String jti, Duration ttl) {
        if (jti == null || jti.isBlank() || ttl == null || ttl.isNegative() || ttl.isZero()) {
            return;
        }
        redis.opsForValue().set(JTI_KEY_PREFIX + jti, "1", ttl);
    }

    @Override
    public void revokeAllIssuedAtOrBefore(UUID userId, Instant revokedAt, Duration ttl) {
        if (userId == null || revokedAt == null || ttl == null || ttl.isNegative() || ttl.isZero()) {
            return;
        }
        String key = USER_KEY_PREFIX + userId;
        String existing = redis.opsForValue().get(key);
        if (existing != null) {
            Instant previous = Instant.parse(existing);
            if (!revokedAt.isAfter(previous)) {
                redis.expire(key, ttl);
                return;
            }
        }
        redis.opsForValue().set(key, revokedAt.toString(), ttl);
    }

    @Override
    public boolean isRevoked(String jti, UUID userId, Instant issuedAt) {
        if (jti != null && !jti.isBlank()) {
            Boolean hasJti = redis.hasKey(JTI_KEY_PREFIX + jti);
            if (Boolean.TRUE.equals(hasJti)) {
                return true;
            }
        }
        if (userId != null && issuedAt != null) {
            String watermark = redis.opsForValue().get(USER_KEY_PREFIX + userId);
            if (watermark != null && !issuedAt.isAfter(Instant.parse(watermark))) {
                return true;
            }
        }
        return false;
    }
}
