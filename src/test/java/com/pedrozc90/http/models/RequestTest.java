package com.pedrozc90.http.models;

import com.pedrozc90.http.enums.HttpMethod;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RequestTest {

    @Test
    void testBuilderCreatesRequest() {
        final Request<String> request = Request.<String>builder()
                .url("https://example.com/api/resource")
                .method(HttpMethod.GET)
                .header("Accept", "application/json")
                .header("Authorization", "Bearer token123")
                .queryParam("page", "1")
                .queryParam("size", "10")
                .body(null)
                .build();

        assertEquals("https://example.com/api/resource", request.getUrl());
        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals(2, request.getHeaders().size());
        assertEquals("application/json", request.getHeaders().get("Accept"));
        assertEquals("Bearer token123", request.getHeaders().get("Authorization"));
        assertEquals(2, request.getQueryParams().size());
        assertEquals("1", request.getQueryParams().get("page"));
        assertNull(request.getBody());
    }

    @Test
    void testBuilderWithBody() {
        final Request<Map<String, String>> request = Request.<Map<String, String>>builder()
                .url("https://example.com/api/resource")
                .method(HttpMethod.POST)
                .header("Content-Type", "application/json")
                .body(Map.of("key", "value"))
                .build();

        assertEquals(HttpMethod.POST, request.getMethod());
        assertNotNull(request.getBody());
        assertEquals("value", request.getBody().get("key"));
    }

    @Test
    void testBuilderMinimal() {
        final Request<Void> request = Request.<Void>builder()
                .url("https://example.com")
                .method(HttpMethod.DELETE)
                .build();

        assertEquals("https://example.com", request.getUrl());
        assertEquals(HttpMethod.DELETE, request.getMethod());
        assertTrue(request.getHeaders().isEmpty());
        assertTrue(request.getQueryParams().isEmpty());
        assertNull(request.getBody());
    }

    @Test
    void testEquality() {
        final Request<Void> r1 = Request.<Void>builder()
                .url("https://example.com")
                .method(HttpMethod.GET)
                .build();

        final Request<Void> r2 = Request.<Void>builder()
                .url("https://example.com")
                .method(HttpMethod.GET)
                .build();

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testToString() {
        final Request<Void> request = Request.<Void>builder()
                .url("https://example.com")
                .method(HttpMethod.GET)
                .build();

        final String str = request.toString();
        assertTrue(str.contains("https://example.com"));
        assertTrue(str.contains("GET"));
    }
}
