package com.pedrozc90.http.objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.pedrozc90.http.enums.HttpMethod;
import com.pedrozc90.http.utils.JsonUtils;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestTest {

    @Test
    public void testConstructorCreatesRequest() {
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
    public void testConstructorWithBody() {
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
    public void testConstructorMinimal() {
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
    public void testPutMethod() {
        final Request<?> request = Request.builder().url("https://example.com").put().build();
        assertEquals(HttpMethod.PUT, request.getMethod());
    }

    @Test
    public void testPatchMethod() {
        final Request<?> request = Request.builder().url("https://example.com").patch().build();
        assertEquals(HttpMethod.PATCH, request.getMethod());
    }

    @Test
    public void testHeadMethod() {
        final Request<?> request = Request.builder().url("https://example.com").head().build();
        assertEquals(HttpMethod.HEAD, request.getMethod());
    }

    @Test
    public void testOptionsMethod() {
        final Request<?> request = Request.builder().url("https://example.com").options().build();
        assertEquals(HttpMethod.OPTIONS, request.getMethod());
    }

    @Test
    public void testTraceMethod() {
        final Request<?> request = Request.builder().url("https://example.com").trace().build();
        assertEquals(HttpMethod.TRACE, request.getMethod());
    }

    @Test
    public void testEquality() {
        final Request<?> r1 = Request.builder().url("https://example.com").get().build();
        final Request<?> r2 = Request.builder().url("https://example.com").get().build();
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    public void testQueryParamsAppendedToUrl() {
        final Request<?> request = Request.builder()
            .url("https://example.com/api/resource")
            .query("page", "1")
            .query("size", "10")
            .get()
            .build();

        final String url = request.getUrl();
        assertTrue(url.startsWith("https://example.com/api/resource?"));
        assertTrue(url.contains("page=1"));
        assertTrue(url.contains("size=10"));
        assertTrue(url.contains("&"));
    }

    @Test
    public void testQueryParamsEncoded() {
        final Request<?> request = Request.builder()
            .url("https://example.com/search")
            .query("q", "hello world")
            .get()
            .build();

        assertTrue(request.getUrl().contains("q=hello+world"));
    }

    @Test
    public void testAcceptHeaderShortcut() {
        final Request<?> request = Request.builder()
            .url("https://example.com")
            .accept("application/json")
            .get()
            .build();

        assertEquals("application/json", request.getHeaders().get("Accept"));
    }

    @Test
    public void testAcceptContentTypeShortcut() {
        final Request<?> request = Request.builder()
            .url("https://example.com")
            .accept(com.pedrozc90.http.enums.ContentType.APPLICATION_JSON)
            .get()
            .build();

        assertEquals("application/json", request.getHeaders().get("Accept"));
    }

    @Test
    public void testBuildWithNullUrlThrowsException() {
        assertThrows(IllegalStateException.class, () ->
            Request.builder().get().build()
        );
    }

    @Test
    public void testBuildWithBlankUrlThrowsException() {
        assertThrows(IllegalStateException.class, () ->
            Request.builder().url("   ").get().build()
        );
    }

    @Test
    public void testBuildWithNullMethodThrowsException() {
        assertThrows(IllegalStateException.class, () -> {
            final RequestBuilder.Builder<?> builder = new RequestBuilder.Builder<>();
            builder.url("https://example.com");
            builder.build();
        });
    }

    @Test
    public void testToString() {
        final Request<?> request = Request.builder().url("https://example.com").get().build();

        final String str = request.toString();
        assertTrue(str.contains("https://example.com"));
        assertTrue(str.contains("GET"));
    }

    @Test
    public void testSerialization() throws JsonProcessingException {
        final Dto dto = new Dto("Pedro");
        final Request<Dto> request = Request.<Dto>builder()
            .url("https://example.com/api/resource")
            .header("Content-Type", "application/json")
            .post()
            .body(dto, Dto.class)
            .build();
        final String result = JsonUtils.toString(request);
        assertTrue(result.contains("\"url\""));
        assertTrue(result.contains("\"https://example.com/api/resource\""));
        assertTrue(result.contains("\"method\""));
        assertTrue(result.contains("\"POST\""));
        assertTrue(result.contains("\"headers\""));
        assertTrue(result.contains("\"body\""));
    }

    @Test
    public void testDeserialization() throws JsonProcessingException {
        final String url = "https://example.com/api/resource";
        final HttpMethod method = HttpMethod.POST;
        final String json = "{\"url\":\"" + url + "\",\"method\":\"" + method + "\",\"headers\":{\"Content-Type\":\"application/json\"},\"body\":{\"name\":\"Pedro\"}}";

        // TODO: error while deserializing request object
        final Request<?> result = JsonUtils.toObject(json, Request.class);
        assertNotNull(result);
        assertEquals(url, result.getUrl());
        assertEquals(method, result.getMethod());
    }

    @Data
    private static class Dto {

        @JsonProperty(value = "name")
        private final String name;

    }

}
