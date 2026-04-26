package com.pedrozc90.http.models;

import com.pedrozc90.http.enums.HttpMethod;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RequestTest {

    @Test
    void testConstructorCreatesRequest() {
        final Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Authorization", "Bearer token123");

        final Request<String> request = new Request<>(
                "https://example.com/api/resource",
                HttpMethod.GET,
                headers,
                null);

        assertEquals("https://example.com/api/resource", request.getUrl());
        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals(2, request.getHeaders().size());
        assertEquals("application/json", request.getHeaders().get("Accept"));
        assertEquals("Bearer token123", request.getHeaders().get("Authorization"));
        assertNull(request.getBody());
    }

    @Test
    void testConstructorWithBody() {
        final Request<Map<String, String>> request = new Request<>(
                "https://example.com/api/resource",
                HttpMethod.POST,
                Collections.singletonMap("Content-Type", "application/json"),
                Map.of("key", "value"));

        assertEquals(HttpMethod.POST, request.getMethod());
        assertNotNull(request.getBody());
        assertEquals("value", request.getBody().get("key"));
    }

    @Test
    void testConstructorMinimal() {
        final Request<Void> request = new Request<>(
                "https://example.com",
                HttpMethod.DELETE,
                Collections.emptyMap(),
                null);

        assertEquals("https://example.com", request.getUrl());
        assertEquals(HttpMethod.DELETE, request.getMethod());
        assertTrue(request.getHeaders().isEmpty());
        assertNull(request.getBody());
    }

    @Test
    void testEquality() {
        final Request<Void> r1 = new Request<>("https://example.com", HttpMethod.GET,
                Collections.emptyMap(), null);
        final Request<Void> r2 = new Request<>("https://example.com", HttpMethod.GET,
                Collections.emptyMap(), null);

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testToString() {
        final Request<Void> request = new Request<>("https://example.com", HttpMethod.GET,
                Collections.emptyMap(), null);

        final String str = request.toString();
        assertTrue(str.contains("https://example.com"));
        assertTrue(str.contains("GET"));
    }
}
