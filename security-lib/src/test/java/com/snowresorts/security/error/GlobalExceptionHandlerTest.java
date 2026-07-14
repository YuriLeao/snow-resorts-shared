package com.snowresorts.security.error;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleApiException maps ResourceNotFoundException to a 404 Problem Detail")
    void handleApiException_resourceNotFound_returns404Problem() {
        // Arrange
        HttpServletRequest request = new MockHttpServletRequest("GET", "/snow-resort-service/v1/resorts/abc-123");
        ResourceNotFoundException ex = ResourceNotFoundException.of("Resort", "abc-123");

        // Act
        ProblemDetail problem = handler.handleApiException(ex, request);

        // Assert
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problem.getTitle()).isEqualTo("Resource Not Found");
        assertThat(problem.getDetail()).contains("abc-123");
        assertThat(problem.getInstance()).hasToString("/snow-resort-service/v1/resorts/abc-123");
    }

    @Test
    @DisplayName("handleApiException maps ConflictException to a 409 Problem Detail")
    void handleApiException_conflict_returns409Problem() {
        // Arrange
        HttpServletRequest request = new MockHttpServletRequest("POST", "/snow-resort-service/v1/resorts/1/reviews");
        ConflictException ex = new ConflictException("You already reviewed this resort.");

        // Act
        ProblemDetail problem = handler.handleApiException(ex, request);

        // Assert
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(problem.getTitle()).isEqualTo("Conflict");
    }

    @Test
    @DisplayName("handleUnexpected hides internal details behind a generic 500 Problem Detail")
    void handleUnexpected_returns500WithoutLeakingDetails() {
        // Arrange
        HttpServletRequest request = new MockHttpServletRequest("GET", "/snow-resort-service/v1/users/me");

        // Act
        ProblemDetail problem = handler.handleUnexpected(new IllegalStateException("boom: secret"), request);

        // Assert
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problem.getDetail()).isEqualTo("An unexpected error occurred.");
        assertThat(problem.getDetail()).doesNotContain("secret");
    }

    @Test
    @DisplayName("handleResponseStatus maps ResponseStatusException to the declared HTTP status")
    void handleResponseStatus_tooManyRequests_returns429Problem() {
        HttpServletRequest request = new MockHttpServletRequest(
                "POST", "/snow-resort-service/v1/location/groups/abc/position");
        ResponseStatusException ex = new ResponseStatusException(
                HttpStatus.TOO_MANY_REQUESTS, "Position publish rate limit exceeded");

        ProblemDetail problem = handler.handleResponseStatus(ex, request);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(problem.getDetail()).isEqualTo("Position publish rate limit exceeded");
    }

    @Test
    @DisplayName("handleApiException maps TooManyRequestsException to a 429 Problem Detail")
    void handleApiException_tooManyRequests_returns429Problem() {
        HttpServletRequest request = new MockHttpServletRequest(
                "POST", "/snow-resort-service/v1/location/groups/abc/position");
        TooManyRequestsException ex = new TooManyRequestsException("Position publish rate limit exceeded");

        ProblemDetail problem = handler.handleApiException(ex, request);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(problem.getTitle()).isEqualTo("Too Many Requests");
    }
}
