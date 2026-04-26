package com.pedrozc90.http.objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pedrozc90.http.enums.HttpMethod;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RequestTest {

    @Test
    void testConstructorCreatesRequest() {
        final Request<?> request = Request.builder()
            .url("https://example.com/api/resource")
            .header("Accept", "application/json")
            .header("Authorization", "Bearer token123")
            .get()
            .build();

        assertEquals("https://example.com/api/resource", request.getUrl());
        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals(2, request.getHeaders().size());
        assertEquals("application/json", request.getHeaders().get("Accept"));
        assertEquals("Bearer token123", request.getHeaders().get("Authorization"));
        assertNull(request.getBody());
    }

    @Test
    void testConstructorWithBody() {
        final Dto dto = new Dto("Pedro");

        final Request<Dto> request = Request.<Dto>builder()
            .url("https://example.com/api/resource")
            .header("Content-Type", "application/json")
            .post()
            .body(dto, Dto.class)
            .build();

        assertEquals(HttpMethod.POST, request.getMethod());
        assertNotNull(request.getBody());
        assertEquals("Pedro", request.getBody().getName());
    }

    @Test
    void testConstructorMinimal() {
        final Request<?> request = Request.builder()
            .url("https://example.com")
            .delete()
            .build();

        assertEquals("https://example.com", request.getUrl());
        assertEquals(HttpMethod.DELETE, request.getMethod());
        assertTrue(request.getHeaders().isEmpty());
        assertNull(request.getBody());
    }

    @Test
    void testEquality() {
        final Request<?> r1 = Request.builder().url("https://example.com").get().build();
        final Request<?> r2 = Request.builder().url("https://example.com").get().build();
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testToString() {
        final Request<?> request = Request.builder().url("https://example.com").get().build();

        final String str = request.toString();
        assertTrue(str.contains("https://example.com"));
        assertTrue(str.contains("GET"));
    }

    @Data
    private static class Dto {

        @JsonProperty(value = "name")
        private final String name;

    }

}
