package com.pedrozc90.http.enums;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class HttpStatusTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testValue() {
        assertEquals(200, HttpStatus.OK.value());
        assertEquals(404, HttpStatus.NOT_FOUND.value());
        assertEquals(500, HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    void testReasonPhrase() {
        assertEquals("OK", HttpStatus.OK.getReasonPhrase());
        assertEquals("Not Found", HttpStatus.NOT_FOUND.getReasonPhrase());
        assertEquals("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
    }

    @Test
    void testToString() {
        assertEquals("200 OK", HttpStatus.OK.toString());
        assertEquals("404 Not Found", HttpStatus.NOT_FOUND.toString());
    }

    @Test
    void testIs1xxInformational() {
        assertTrue(HttpStatus.CONTINUE.is1xxInformational());
        assertFalse(HttpStatus.OK.is1xxInformational());
    }

    @Test
    void testIs2xxSuccessful() {
        assertTrue(HttpStatus.OK.is2xxSuccessful());
        assertTrue(HttpStatus.CREATED.is2xxSuccessful());
        assertTrue(HttpStatus.NO_CONTENT.is2xxSuccessful());
        assertFalse(HttpStatus.BAD_REQUEST.is2xxSuccessful());
        assertFalse(HttpStatus.INTERNAL_SERVER_ERROR.is2xxSuccessful());
    }

    @Test
    void testIs3xxRedirection() {
        assertTrue(HttpStatus.MOVED_PERMANENTLY.is3xxRedirection());
        assertTrue(HttpStatus.NOT_MODIFIED.is3xxRedirection());
        assertFalse(HttpStatus.OK.is3xxRedirection());
    }

    @Test
    void testIs4xxClientError() {
        assertTrue(HttpStatus.BAD_REQUEST.is4xxClientError());
        assertTrue(HttpStatus.NOT_FOUND.is4xxClientError());
        assertTrue(HttpStatus.UNAUTHORIZED.is4xxClientError());
        assertFalse(HttpStatus.OK.is4xxClientError());
        assertFalse(HttpStatus.INTERNAL_SERVER_ERROR.is4xxClientError());
    }

    @Test
    void testIs5xxServerError() {
        assertTrue(HttpStatus.INTERNAL_SERVER_ERROR.is5xxServerError());
        assertTrue(HttpStatus.BAD_GATEWAY.is5xxServerError());
        assertFalse(HttpStatus.OK.is5xxServerError());
        assertFalse(HttpStatus.NOT_FOUND.is5xxServerError());
    }

    @Test
    void testIsError() {
        assertTrue(HttpStatus.BAD_REQUEST.isError());
        assertTrue(HttpStatus.INTERNAL_SERVER_ERROR.isError());
        assertFalse(HttpStatus.OK.isError());
        assertFalse(HttpStatus.CREATED.isError());
    }

    @Test
    void testResolve() {
        assertEquals(HttpStatus.OK, HttpStatus.resolve(200));
        assertEquals(HttpStatus.NOT_FOUND, HttpStatus.resolve(404));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.resolve(500));
    }

    @Test
    void testResolveUnknownThrows() {
        assertThrows(IllegalArgumentException.class, () -> HttpStatus.resolve(999));
    }

    @Test
    void testSerializer() throws IOException {
        final String json = objectMapper.writeValueAsString(HttpStatus.OK);
        assertEquals("200", json);

        final String json404 = objectMapper.writeValueAsString(HttpStatus.NOT_FOUND);
        assertEquals("404", json404);
    }

    @Test
    void testDeserializer() throws IOException {
        assertEquals(HttpStatus.OK, objectMapper.readValue("200", HttpStatus.class));
        assertEquals(HttpStatus.NOT_FOUND, objectMapper.readValue("404", HttpStatus.class));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, objectMapper.readValue("500", HttpStatus.class));
    }

    @Test
    void testDeserializerUnknownThrows() {
        assertThrows(Exception.class, () -> objectMapper.readValue("999", HttpStatus.class));
    }
}
