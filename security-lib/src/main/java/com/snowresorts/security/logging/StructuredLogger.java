package com.snowresorts.security.logging;

import org.slf4j.Logger;
import org.slf4j.spi.LoggingEventBuilder;

/**
 * Thin wrapper over the SLF4J fluent API for consistent structured fields:
 * {@code event}, {@code outcome}, {@code reason}, plus optional key/value pairs.
 *
 * <p>Example:
 * <pre>{@code
 * StructuredLogger.of(log).info("login", "denied", "invalid_credentials",
 *         "user_id", accountId);
 * }</pre>
 */
public final class StructuredLogger {

    private final Logger log;

    private StructuredLogger(Logger log) {
        this.log = log;
    }

    public static StructuredLogger of(Logger log) {
        return new StructuredLogger(log);
    }

    public void info(String event, String outcome, String reason, Object... keyValues) {
        emit(log.atInfo(), event, outcome, reason, keyValues);
    }

    public void warn(String event, String outcome, String reason, Object... keyValues) {
        emit(log.atWarn(), event, outcome, reason, keyValues);
    }

    public void error(String event, String outcome, String reason, Throwable cause, Object... keyValues) {
        LoggingEventBuilder builder = log.atError()
                .addKeyValue("event", event)
                .addKeyValue("outcome", outcome)
                .addKeyValue("reason", reason)
                .setCause(cause);
        addPairs(builder, keyValues);
        builder.setMessage(event + " " + outcome).log();
    }

    public void debug(String event, String outcome, String reason, Object... keyValues) {
        emit(log.atDebug(), event, outcome, reason, keyValues);
    }

    private static void emit(LoggingEventBuilder builder, String event, String outcome, String reason,
                             Object... keyValues) {
        builder = builder
                .addKeyValue("event", event)
                .addKeyValue("outcome", outcome)
                .addKeyValue("reason", reason);
        addPairs(builder, keyValues);
        builder.setMessage(event + " " + outcome).log();
    }

    private static void addPairs(LoggingEventBuilder builder, Object... keyValues) {
        if (keyValues == null || keyValues.length == 0) {
            return;
        }
        if ((keyValues.length & 1) != 0) {
            throw new IllegalArgumentException("keyValues must be an even number of arguments (key, value)*");
        }
        for (int i = 0; i < keyValues.length; i += 2) {
            Object key = keyValues[i];
            Object value = keyValues[i + 1];
            if (key != null) {
                builder.addKeyValue(String.valueOf(key), value);
            }
        }
    }
}
