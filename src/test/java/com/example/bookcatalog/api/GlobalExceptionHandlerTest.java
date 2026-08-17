package com.example.bookcatalog.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void unexpectedFailureUsesStandardNonLeakingResponse() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/books");

        ResponseEntity<ApiError> response = handler.handleUnexpected(
                new RuntimeException("sensitive internal detail"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().message()).isEqualTo("An unexpected error occurred");
        assertThat(response.getBody().message()).doesNotContain("sensitive");
        assertThat(response.getBody().path()).isEqualTo("/api/books");
        assertThat(response.getBody().fieldErrors()).isEqualTo(Map.of());
    }
}
