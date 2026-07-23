package com.snowresorts.security.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class StructuredLoggerTest {

    @Test
    @DisplayName("info emits a structured message with event outcome and reason")
    void info_emitsStructuredFields() {
        Logger logger = (Logger) LoggerFactory.getLogger("structured-logger-test");
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        StructuredLogger.of(logger).info("login", "denied", "invalid_credentials",
                "user_id", "11111111-1111-1111-1111-111111111111");

        assertThat(appender.list).hasSize(1);
        ILoggingEvent event = appender.list.getFirst();
        assertThat(event.getFormattedMessage()).contains("login denied");
        assertThat(event.getKeyValuePairs()).isNotNull();
        assertThat(event.getKeyValuePairs().stream().map(kv -> kv.key + "=" + kv.value))
                .anyMatch(s -> s.equals("event=login"))
                .anyMatch(s -> s.equals("outcome=denied"))
                .anyMatch(s -> s.equals("reason=invalid_credentials"))
                .anyMatch(s -> s.contains("user_id="));

        logger.detachAppender(appender);
    }

    @Test
    @DisplayName("odd number of keyValues is rejected")
    void info_withOddKeyValues_throws() {
        Logger logger = (Logger) LoggerFactory.getLogger("structured-logger-odd");
        assertThatThrownBy(() -> StructuredLogger.of(logger).info("e", "o", "r", "only-key"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
