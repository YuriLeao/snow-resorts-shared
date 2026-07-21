package com.snowresorts.security.jwt;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Process-local revocation store. Sufficient for single-task deployments (e.g. staging)
 * and unit tests; multi-instance prod should use the Redis-backed store.
 */
public class InMemoryAccessTokenRevocationStore implements AccessTokenRevocationStore {

    private final Map<String, Instant> revokedJtis = new ConcurrentHashMap<>();
    private final Map<UUID, Instant> userRevokedBefore = new ConcurrentHashMap<>();

    @Override
    public void revokeJti(String jti, Duration ttl) {
        if (jti == null || jti.isBlank()) {
            return;
        }
        revokedJtis.put(jti, Instant.now().plus(ttl));
        purgeExpired();
    }

    @Override
    public void revokeAllIssuedAtOrBefore(UUID userId, Instant revokedAt, Duration ttl) {
        if (userId == null || revokedAt == null) {
            return;
        }
        userRevokedBefore.merge(userId, revokedAt, (existing, incoming) ->
                incoming.isAfter(existing) ? incoming : existing);
        purgeExpired();
    }

    @Override
    public boolean isRevoked(String jti, UUID userId, Instant issuedAt) {
        purgeExpired();
        if (jti != null && !jti.isBlank()) {
            Instant jtiExpiry = revokedJtis.get(jti);
            if (jtiExpiry != null && Instant.now().isBefore(jtiExpiry)) {
                return true;
            }
        }
        if (userId != null && issuedAt != null) {
            Instant watermark = userRevokedBefore.get(userId);
            if (watermark != null && !issuedAt.isAfter(watermark)) {
                return true;
            }
        }
        return false;
    }

    private void purgeExpired() {
        Instant now = Instant.now();
        revokedJtis.entrySet().removeIf(e -> !now.isBefore(e.getValue()));
    }
}
