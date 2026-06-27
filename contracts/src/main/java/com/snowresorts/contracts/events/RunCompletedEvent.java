package com.snowresorts.contracts.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Emitted by activity-service when a descent is finalised. Consumed to refresh friend
 * leaderboard caches and (later) trigger achievements/notifications.
 */
public record RunCompletedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID runId,
        UUID userId,
        UUID resortId,
        double maxSpeedKmh,
        double distanceM,
        long durationSec) implements DomainEvent {

    public static final String TYPE = "run.completed";

    @Override
    public String type() {
        return TYPE;
    }
}
