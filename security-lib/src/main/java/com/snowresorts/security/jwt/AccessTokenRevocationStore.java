package com.snowresorts.security.jwt;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Tracks revoked access tokens so resource servers can reject JWTs after logout
 * (or password reset) before their natural expiry.
 */
public interface AccessTokenRevocationStore {

    /** Marks a single JWT {@code jti} as revoked until {@code ttl} elapses. */
    void revokeJti(String jti, Duration ttl);

    /**
     * Marks every access token issued for {@code userId} at or before {@code revokedAt}
     * as invalid (session kill).
     */
    void revokeAllIssuedAtOrBefore(UUID userId, Instant revokedAt, Duration ttl);

    /** @return true when the JWT must be rejected. */
    boolean isRevoked(String jti, UUID userId, Instant issuedAt);
}
