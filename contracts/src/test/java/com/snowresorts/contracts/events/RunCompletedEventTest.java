package com.snowresorts.contracts.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RunCompletedEventTest {

    @Test
    @DisplayName("type returns the stable 'run.completed' discriminator")
    void type_returnsStableDiscriminator() {
        RunCompletedEvent event = new RunCompletedEvent(
                UUID.randomUUID(), Instant.now(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), 82.4, 1340.0, 95L);

        assertEquals("run.completed", event.type());
        assertEquals(RunCompletedEvent.TYPE, event.type());
    }
}
