package com.pedrozc90.http.objects;

import com.pedrozc90.http.enums.HttpStatus;
import com.pedrozc90.http.exceptions.JsonException;
import com.pedrozc90.http.utils.JsonUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
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
        assertEquals(200, buildResponse(HttpStatus.OK).getStatus().code());
        assertEquals(404, buildResponse(HttpStatus.NOT_FOUND).getStatus().code());
        assertEquals(500, buildResponse(HttpStatus.INTERNAL_SERVER_ERROR).getStatus().code());
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

    @Test
    public void testSerialization() throws JsonException {
        final long start = System.currentTimeMillis();
        final Map<String, String> headers = Collections.singletonMap("header", "value");
        final String payload = "Sanity Check";

        final Response response = Response.of(HttpStatus.NOT_FOUND, headers, payload.getBytes(), start);

        final String result = JsonUtils.toString(response);
        assertTrue(result.contains("\"status\" : 404"));
        assertTrue(result.contains("\"headers\""));
        assertTrue(result.contains("\"payload\""));
    }

    @Test
    public void testDeserialization() throws JsonException {
        final String json = "{\"status\":404,\"headers\":{\"header\":\"value\"},\"payload\":\"Sanity Check\"}";

        final Response result = JsonUtils.toObject(json, Response.class);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatus());
    }

    @Test
    void testIsJson_true() {
        final Response response = new Response(HttpStatus.OK,
            Collections.singletonMap("Content-Type", "application/json"), null, 0);
        assertTrue(response.isJson());
    }

    @Test
    void testIsJson_withCharset() {
        final Response response = new Response(HttpStatus.OK,
            Collections.singletonMap("Content-Type", "application/json; charset=UTF-8"), null, 0);
        assertTrue(response.isJson());
    }

    @Test
    void testIsJson_false() {
        final Response response = new Response(HttpStatus.OK,
            Collections.singletonMap("Content-Type", "text/plain"), null, 0);
        assertFalse(response.isJson());
    }

    @Test
    void testIsJson_noHeaders() {
        final Response response = new Response(HttpStatus.OK, null, null, 0);
        assertFalse(response.isJson());
    }

    @Test
    void testIsJson_problemJson() {
        final Response response = new Response(HttpStatus.OK,
            Collections.singletonMap("Content-Type", "application/problem+json"), null, 0);
        assertTrue(response.isJson());
    }

    @Test
    void testIsJson_vndApiJson() {
        final Response response = new Response(HttpStatus.OK,
            Collections.singletonMap("Content-Type", "application/vnd.api+json"), null, 0);
        assertTrue(response.isJson());
    }

    @Test
    void testIsFile_true() {
        final Response response = new Response(HttpStatus.OK,
            Collections.singletonMap("Content-Disposition", "attachment; filename=\"file.pdf\""), null, 0);
        assertTrue(response.isFile());
    }

    @Test
    void testIsFile_false() {
        final Response response = new Response(HttpStatus.OK,
            Collections.singletonMap("Content-Type", "application/json"), null, 0);
        assertFalse(response.isFile());
    }

    @Test
    void testIsFile_noHeaders() {
        final Response response = new Response(HttpStatus.OK, null, null, 0);
        assertFalse(response.isFile());
    }

    @Test
    void testGetCharset_fromHeader() {
        final Response response = new Response(HttpStatus.OK,
            Collections.singletonMap("Content-Type", "text/html; charset=ISO-8859-1"), null, 0);
        assertEquals(Charset.forName("ISO-8859-1"), response.getCharset());
    }

    @Test
    void testGetCharset_defaultsToUtf8() {
        final Response response = new Response(HttpStatus.OK,
            Collections.singletonMap("Content-Type", "application/json"), null, 0);
        assertEquals(StandardCharsets.UTF_8, response.getCharset());
    }

    @Test
    void testGetCharset_noHeaders() {
        final Response response = new Response(HttpStatus.OK, null, null, 0);
        assertEquals(StandardCharsets.UTF_8, response.getCharset());
    }

    @Test
    void testGetLength_withPayload() {
        final byte[] payload = "hello".getBytes(StandardCharsets.UTF_8);
        final Response response = new Response(HttpStatus.OK, null, payload, 0);
        assertEquals(5, response.getLength());
    }

    @Test
    void testGetLength_nullPayload() {
        final Response response = new Response(HttpStatus.OK, null, null, 0);
        assertEquals(0, response.getLength());
    }

    @Test
    void testAsString_usesHeaderCharset() {
        final byte[] payload = "héllo".getBytes(StandardCharsets.ISO_8859_1);
        final Response response = new Response(HttpStatus.OK,
            Collections.singletonMap("Content-Type", "text/plain; charset=ISO-8859-1"),
            payload, 0);
        assertEquals("héllo", response.asString());
    }

    @Test
    void testIsFile_inlineDispositionNotFile() {
        final Response response = new Response(HttpStatus.OK,
            Collections.singletonMap("Content-Disposition", "inline; filename=\"file.pdf\""), null, 0);
        assertFalse(response.isFile());
    }

    @Test
    void testAsFile_nullPayloadThrowsIOException() {
        final Response response = new Response(HttpStatus.OK,
            Collections.singletonMap("Content-Disposition", "attachment; filename=\"file.txt\""),
            null, 0);
        assertThrows(java.io.IOException.class, response::asFile);
    }

    /* --- Helpers --- */
    private static Response buildResponse(final HttpStatus status) {
        return new Response(status, Collections.emptyMap(), null, 0);
    }
}
