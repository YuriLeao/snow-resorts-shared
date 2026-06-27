package com.snowresorts.contracts.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Emitted by resort-service when a resort review is created or updated, so the cached
 * aggregate rating ({@code resorts.avg_rating}, {@code review_count}) can be recomputed.
 */
public record ReviewCreatedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID reviewId,
        UUID resortId,
        UUID userId,
        int rating) implements DomainEvent {

    public static final String TYPE = "review.created";

    @Override
    public String type() {
        return TYPE;
    }
}
