package com.pedrozc90.http.enums;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class HttpStatusTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testCode() {
        assertEquals(200, HttpStatus.OK.code());
        assertEquals(404, HttpStatus.NOT_FOUND.code());
        assertEquals(500, HttpStatus.INTERNAL_SERVER_ERROR.code());
    }

    @Test
    void testReasonPhrase() {
        assertEquals("OK", HttpStatus.OK.reason());
        assertEquals("Not Found", HttpStatus.NOT_FOUND.reason());
        assertEquals("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.reason());
    }

    @Test
    void testToString() {
        assertEquals("200 OK", HttpStatus.OK.toString());
        assertEquals("404 Not Found", HttpStatus.NOT_FOUND.toString());
    }

    @Test
    void testIsInformational() {
        assertTrue(HttpStatus.CONTINUE.isInformational());
        assertFalse(HttpStatus.OK.isInformational());
    }

    @Test
    void testIsSuccessful() {
        assertTrue(HttpStatus.OK.isSuccessful());
        assertTrue(HttpStatus.CREATED.isSuccessful());
        assertTrue(HttpStatus.NO_CONTENT.isSuccessful());
        assertFalse(HttpStatus.BAD_REQUEST.isSuccessful());
        assertFalse(HttpStatus.INTERNAL_SERVER_ERROR.isSuccessful());
    }

    @Test
    void testIs3xxRedirection() {
        assertTrue(HttpStatus.MOVED_PERMANENTLY.isRedirection());
        assertTrue(HttpStatus.NOT_MODIFIED.isRedirection());
        assertFalse(HttpStatus.OK.isRedirection());
    }

    @Test
    void testIsClientError() {
        assertTrue(HttpStatus.BAD_REQUEST.isClientError());
        assertTrue(HttpStatus.NOT_FOUND.isClientError());
        assertTrue(HttpStatus.UNAUTHORIZED.isClientError());
        assertFalse(HttpStatus.OK.isClientError());
        assertFalse(HttpStatus.INTERNAL_SERVER_ERROR.isClientError());
    }

    @Test
    void testIsServerError() {
        assertTrue(HttpStatus.INTERNAL_SERVER_ERROR.isServerError());
        assertTrue(HttpStatus.BAD_GATEWAY.isServerError());
        assertFalse(HttpStatus.OK.isServerError());
        assertFalse(HttpStatus.NOT_FOUND.isServerError());
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
        assertEquals(HttpStatus.NONE, HttpStatus.resolve(-1));
    }

    @Test
    void testResolveUnknownReturnsNone() {
        assertEquals(HttpStatus.NONE, HttpStatus.resolve(999));
    }

    @Test
    void testNoneIsNotAnyErrorOrSuccess() {
        assertFalse(HttpStatus.NONE.isSuccessful());
        assertFalse(HttpStatus.NONE.isClientError());
        assertFalse(HttpStatus.NONE.isServerError());
        assertFalse(HttpStatus.NONE.isError());
        assertTrue(HttpStatus.NONE.isNone());
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
    void testDeserializerUnknownReturnsNone() throws IOException {
        assertEquals(HttpStatus.NONE, objectMapper.readValue("999", HttpStatus.class));
    }
}
