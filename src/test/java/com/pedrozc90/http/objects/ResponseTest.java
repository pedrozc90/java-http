package com.pedrozc90.http.objects;

import com.pedrozc90.http.enums.HttpStatus;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class ResponseTest {

    @Test
    void testConstructorCreatesResponse() {
        final Response response = new Response(
            HttpStatus.OK,
            Collections.singletonMap("Content-Type", "application/json"),
            "{\"message\":\"hello\"}".getBytes(),
            0
        );

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals(1, response.getHeaders().size());
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
        assertEquals("{\"message\":\"hello\"}", response.asString());
    }

    @Test
    void testIsSuccessful() {
        assertTrue(buildResponse(HttpStatus.OK).isSuccessful());
        assertTrue(buildResponse(HttpStatus.CREATED).isSuccessful());
        assertTrue(buildResponse(HttpStatus.NO_CONTENT).isSuccessful());
        assertFalse(buildResponse(HttpStatus.BAD_REQUEST).isSuccessful());
        assertFalse(buildResponse(HttpStatus.NOT_FOUND).isSuccessful());
        assertFalse(buildResponse(HttpStatus.INTERNAL_SERVER_ERROR).isSuccessful());
    }

    @Test
    void testIsClientError() {
        assertTrue(buildResponse(HttpStatus.BAD_REQUEST).isClientError());
        assertTrue(buildResponse(HttpStatus.NOT_FOUND).isClientError());
        assertTrue(buildResponse(HttpStatus.UNAUTHORIZED).isClientError());
        assertFalse(buildResponse(HttpStatus.OK).isClientError());
        assertFalse(buildResponse(HttpStatus.INTERNAL_SERVER_ERROR).isClientError());
    }

    @Test
    void testIsServerError() {
        assertTrue(buildResponse(HttpStatus.INTERNAL_SERVER_ERROR).isServerError());
        assertTrue(buildResponse(HttpStatus.BAD_GATEWAY).isServerError());
        assertTrue(buildResponse(HttpStatus.SERVICE_UNAVAILABLE).isServerError());
        assertFalse(buildResponse(HttpStatus.OK).isServerError());
        assertFalse(buildResponse(HttpStatus.NOT_FOUND).isServerError());
    }

    @Test
    void testIsError() {
        assertTrue(buildResponse(HttpStatus.BAD_REQUEST).isError());
        assertTrue(buildResponse(HttpStatus.INTERNAL_SERVER_ERROR).isError());
        assertFalse(buildResponse(HttpStatus.OK).isError());
        assertFalse(buildResponse(HttpStatus.CREATED).isError());
    }

    @Test
    void testGetStatusCode() {
        assertEquals(200, buildResponse(HttpStatus.OK).getStatus().value());
        assertEquals(404, buildResponse(HttpStatus.NOT_FOUND).getStatus().value());
        assertEquals(500, buildResponse(HttpStatus.INTERNAL_SERVER_ERROR).getStatus().value());
    }

    @Test
    void testGetStatusCodeWithNullStatus() {
        final Response response = new Response(null, null, null, 0);
        assertNull(response.getStatus());
        assertFalse(response.isSuccessful());
        assertFalse(response.isClientError());
        assertFalse(response.isServerError());
        assertFalse(response.isError());
    }

    @Test
    void testEquality() {
        final Response r1 = new Response(HttpStatus.OK, Collections.emptyMap(), "body".getBytes(), 0);
        final Response r2 = new Response(HttpStatus.OK, Collections.emptyMap(), "body".getBytes(), 0);

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testToString() {
        final Response response = buildResponse(HttpStatus.OK);
        final String str = response.toString();
        assertTrue(str.contains("OK"));
    }

    /* --- Helpers --- */
    private static Response buildResponse(final HttpStatus status) {
        return new Response(status, Collections.emptyMap(), null, 0);
    }
}
