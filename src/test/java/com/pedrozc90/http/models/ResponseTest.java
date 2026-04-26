package com.pedrozc90.http.models;

import com.pedrozc90.http.enums.HttpStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResponseTest {

    @Test
    void testBuilderCreatesResponse() {
        final Response<String> response = Response.<String>builder()
                .status(HttpStatus.OK)
                .reason(HttpStatus.OK.getReasonPhrase())
                .header("Content-Type", "application/json")
                .body("{\"message\":\"hello\"}")
                .build();

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("OK", response.getReason());
        assertEquals(1, response.getHeaders().size());
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
        assertEquals("{\"message\":\"hello\"}", response.getBody());
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
        assertEquals(200, buildResponse(HttpStatus.OK).getStatusCode());
        assertEquals(404, buildResponse(HttpStatus.NOT_FOUND).getStatusCode());
        assertEquals(500, buildResponse(HttpStatus.INTERNAL_SERVER_ERROR).getStatusCode());
    }

    @Test
    void testGetStatusCodeWithNullStatus() {
        final Response<Void> response = Response.<Void>builder().build();
        assertEquals(-1, response.getStatusCode());
        assertFalse(response.isSuccessful());
        assertFalse(response.isClientError());
        assertFalse(response.isServerError());
        assertFalse(response.isError());
    }

    @Test
    void testEquality() {
        final Response<String> r1 = Response.<String>builder()
                .status(HttpStatus.OK)
                .body("body")
                .build();

        final Response<String> r2 = Response.<String>builder()
                .status(HttpStatus.OK)
                .body("body")
                .build();

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testToString() {
        final Response<Void> response = buildResponse(HttpStatus.OK);
        final String str = response.toString();
        assertTrue(str.contains("OK"));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static Response<Void> buildResponse(final HttpStatus status) {
        return Response.<Void>builder()
                .status(status)
                .reason(status.getReasonPhrase())
                .build();
    }
}
