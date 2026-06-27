package com.snowresorts.contracts.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Marker for asynchronous domain events exchanged between services (SNS/SQS in AWS,
 * in-process or Redis in dev). Implementations are immutable {@code record}s.
 */
public interface DomainEvent {

    /** Stable, unique identifier for idempotent consumers. */
    UUID eventId();

    /** Logical event type, e.g. {@code "run.completed"}. */
    String type();

    /** Wall-clock time the event was produced. */
    Instant occurredAt();
}
