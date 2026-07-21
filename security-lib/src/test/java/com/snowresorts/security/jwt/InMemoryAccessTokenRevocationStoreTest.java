package com.snowresorts.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InMemoryAccessTokenRevocationStoreTest {

    @Test
    @DisplayName("revokeAllIssuedAtOrBefore rejects tokens issued at or before the watermark")
    void revokeAll_rejectsTokensAtOrBeforeWatermark() {
        InMemoryAccessTokenRevocationStore store = new InMemoryAccessTokenRevocationStore();
        UUID userId = UUID.randomUUID();
        Instant issuedAt = Instant.parse("2026-07-21T12:00:00Z");
        Instant logoutAt = Instant.parse("2026-07-21T12:05:00Z");

        store.revokeAllIssuedAtOrBefore(userId, logoutAt, Duration.ofMinutes(15));

        assertThat(store.isRevoked(null, userId, issuedAt)).isTrue();
        assertThat(store.isRevoked(null, userId, logoutAt)).isTrue();
        assertThat(store.isRevoked(null, userId, logoutAt.plusSeconds(1))).isFalse();
    }

    @Test
    @DisplayName("revokeJti rejects that jti until ttl elapses")
    void revokeJti_rejectsMatchingJti() {
        InMemoryAccessTokenRevocationStore store = new InMemoryAccessTokenRevocationStore();

        store.revokeJti("jti-1", Duration.ofMinutes(15));

        assertThat(store.isRevoked("jti-1", null, null)).isTrue();
        assertThat(store.isRevoked("jti-other", null, null)).isFalse();
    }
}
