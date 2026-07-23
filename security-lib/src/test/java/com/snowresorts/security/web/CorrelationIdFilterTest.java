package com.snowresorts.security.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

class CorrelationIdFilterTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    @DisplayName("uses X-Trace-Id header for request_id and traceId MDC keys")
    void doFilter_withHeader_setsMdcAndResponseHeader() throws Exception {
        CorrelationIdFilter filter = new CorrelationIdFilter();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getHeader(CorrelationIdFilter.TRACE_ID_HEADER)).thenReturn("req-123");

        doAnswer(invocation -> {
            assertThat(MDC.get(CorrelationIdFilter.MDC_REQUEST_ID)).isEqualTo("req-123");
            assertThat(MDC.get(CorrelationIdFilter.MDC_TRACE_ID)).isEqualTo("req-123");
            return null;
        }).when(chain).doFilter(any(), any());

        filter.doFilter(request, response, chain);

        verify(response).setHeader(CorrelationIdFilter.TRACE_ID_HEADER, "req-123");
        assertThat(MDC.get(CorrelationIdFilter.MDC_REQUEST_ID)).isNull();
        assertThat(MDC.get(CorrelationIdFilter.MDC_TRACE_ID)).isNull();
    }

    @Test
    @DisplayName("generates a request_id when header is absent")
    void doFilter_withoutHeader_generatesRequestId() throws Exception {
        CorrelationIdFilter filter = new CorrelationIdFilter();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getHeader(CorrelationIdFilter.TRACE_ID_HEADER)).thenReturn(null);

        final String[] captured = new String[1];
        doAnswer(invocation -> {
            captured[0] = MDC.get(CorrelationIdFilter.MDC_REQUEST_ID);
            assertThat(captured[0]).isNotBlank();
            assertThat(MDC.get(CorrelationIdFilter.MDC_TRACE_ID)).isEqualTo(captured[0]);
            return null;
        }).when(chain).doFilter(any(), any());

        filter.doFilter(request, response, chain);

        verify(response).setHeader(CorrelationIdFilter.TRACE_ID_HEADER, captured[0]);
    }
}
