package com.pedrozc90.http.exceptions;

import com.pedrozc90.http.enums.HttpMethod;
import com.pedrozc90.http.enums.HttpStatus;
import com.pedrozc90.http.objects.Request;
import com.pedrozc90.http.objects.Response;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class HttpResponseExceptionTest {

    private static final String URL = "https://api.example.com/users";

    private Request<?> request(final HttpMethod method) {
        return new Request<>(URL, method, Collections.emptyMap(), null, null);
    }

    private Response response(final HttpStatus status) {
        return new Response(status, Collections.emptyMap(), null, 0);
    }

    @Test
    void explicitMessageIsPreserved() {
        final HttpResponseException ex = new HttpResponseException(
            "custom message", request(HttpMethod.GET), response(HttpStatus.NOT_FOUND));
        assertEquals("custom message", ex.getMessage());
    }

    @Test
    void defaultMessageContainsStatusAndMethodAndUrl() {
        final HttpResponseException ex = new HttpResponseException(
            new RuntimeException("boom"), request(HttpMethod.POST), response(HttpStatus.INTERNAL_SERVER_ERROR));

        final String message = ex.getMessage();
        assertNotNull(message);
        assertTrue(message.contains("500"), "message should include status code");
        assertTrue(message.contains("POST"), "message should include HTTP method");
        assertTrue(message.contains(URL), "message should include URL");
    }

    @Test
    void defaultMessageHandlesNullRequest() {
        final HttpResponseException ex = new HttpResponseException(
            new RuntimeException("boom"), null, response(HttpStatus.BAD_GATEWAY));

        final String message = ex.getMessage();
        assertNotNull(message);
        assertTrue(message.contains("502"), "message should include status code");
    }

    @Test
    void defaultMessageHandlesNullResponse() {
        final HttpResponseException ex = new HttpResponseException(
            new RuntimeException("boom"), request(HttpMethod.GET), null);

        final String message = ex.getMessage();
        assertNotNull(message);
        assertTrue(message.contains("GET"), "message should include HTTP method");
        assertTrue(message.contains(URL), "message should include URL");
    }

}
