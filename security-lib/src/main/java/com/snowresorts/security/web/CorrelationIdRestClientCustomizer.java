package com.snowresorts.security.web;

import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

/**
 * Propagates {@code X-Trace-Id} from the current MDC onto outbound {@link RestClient} calls.
 */
@AutoConfiguration
@ConditionalOnClass(RestClient.class)
public class CorrelationIdRestClientCustomizer {

    @Bean
    RestClientCustomizer correlationIdRestClientCustomizer() {
        return builder -> builder.requestInterceptor((request, body, execution) -> {
            String requestId = MDC.get(CorrelationIdFilter.MDC_REQUEST_ID);
            if (requestId != null && !requestId.isBlank()
                    && !request.getHeaders().containsKey(CorrelationIdFilter.TRACE_ID_HEADER)) {
                request.getHeaders().set(CorrelationIdFilter.TRACE_ID_HEADER, requestId);
            }
            return execution.execute(request, body);
        });
    }
}
