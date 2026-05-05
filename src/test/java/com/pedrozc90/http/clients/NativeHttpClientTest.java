package com.pedrozc90.http.clients;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.pedrozc90.http.enums.ContentType;
import com.pedrozc90.http.enums.HttpStatus;
import com.pedrozc90.http.exceptions.HttpResponseException;
import com.pedrozc90.http.objects.HttpFile;
import com.pedrozc90.http.objects.Request;
import com.pedrozc90.http.objects.Response;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

public class NativeHttpClientTest {

    @RegisterExtension
    private static final WireMockExtension wiremock = WireMockExtension.newInstance()
        .options(
            WireMockConfiguration.options()
                .dynamicPort()
                .notifier(new ConsoleNotifier(false))
        )
        .build();

    private final NativeHttpClient client = new NativeHttpClient();

    @Test
    void shouldReturn200Response() throws HttpResponseException, IOException {
        // stub '/get' endpoint
        wiremock.stubFor(
            get("/get")
                .withHeader("Content-Type", containing("application/json"))
                .willReturn(
                    ok()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("sanity-check.json")
                )
        );

        final Request<?> request = Request.builder()
            .url(wiremock.url("/get"))
            .contentType(ContentType.APPLICATION_JSON)
            .get()
            .build();

        final Response response = client.execute(request);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());

        final byte[] body = response.getPayload();
        assertNotNull(body);

        final Result result = response.as(Result.class);
        assertNotNull(result);
        assertEquals("Sanity Check", result.getMessage());
    }

    @Test
    void shouldReturnHtml() throws HttpResponseException, IOException {
        // stub '/get' endpoint
        wiremock.stubFor(
            get("/html")
                .withHeader("Content-Type", containing("text/html"))
                .willReturn(
                    ok()
                        .withHeader("Content-Type", "text/html")
                        .withBodyFile("sanity-check.html")
                )
        );

        final Request<?> request = Request.builder()
            .url(wiremock.url("/html"))
            .contentType(ContentType.TEXT_HTML)
            .get()
            .build();

        final Response response = client.execute(request);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());

        final byte[] body = response.getPayload();
        assertNotNull(body);

        final String result = response.asString();
        assertNotNull(result);
    }

    @Test
    void shouldDownloadAFile() throws HttpResponseException, IOException {
        // stub '/get' endpoint
        wiremock.stubFor(
            get("/txt")
                .withHeader("Content-Type", containing("text/plain"))
                .willReturn(
                    ok()
                        .withHeader("Content-Type", "text/plain")
                        .withHeader("Content-Disposition", "attachment; filename=\"sanity-check.txt\"")
                        .withBodyFile("sanity-check.txt")
                )
        );

        final Request<?> request = Request.builder()
            .url(wiremock.url("/txt"))
            .contentType(ContentType.TEXT_PLAIN)
            .get()
            .build();

        final Response response = client.execute(request);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());

        final byte[] body = response.getPayload();
        assertNotNull(body);

        final HttpFile result = response.asFile();
        assertNotNull(result);
        assertNotNull(result.getFile());
        assertTrue(result.getFile().exists());
        assertEquals("sanity-check.txt", result.getFilename());
        assertTrue(result.getContentType().contains("text/plain"));
        assertTrue(result.getSize() > 0);

        result.getFile().delete();
    }

    @Test
    void shouldReturn404Response() throws HttpResponseException, IOException {
        // stub '/' endpoint
        wiremock.stubFor(get("/"));

        final HttpResponseException cause = assertThrows(HttpResponseException.class, () -> {
            final Request<?> request = Request.builder()
                .url(wiremock.url("/not-found"))
                .get()
                .build();

            final Response response = client.execute(request);
            assertNotNull(response);
        });

        final Request<?> request = cause.getRequest();
        assertNotNull(request);

        final Response response = cause.getResponse();
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
        assertNotNull(response.getPayload());

        // the exception message is always set (either from the HTTP status line or auto-generated)
        assertNotNull(cause.getMessage());
    }

    // -------------------------------------------------------------------------
    // Retry policy tests
    // -------------------------------------------------------------------------

    @Test
    void retryShouldSucceedOnFirstAttemptWhenPolicySet() throws HttpResponseException {
        wiremock.stubFor(
            get("/ok-retry")
                .willReturn(ok().withHeader("Content-Type", "application/json").withBody("{}")));

        final Request<?> request = Request.builder()
            .url(wiremock.url("/ok-retry"))
            .contentType(ContentType.APPLICATION_JSON)
            .retryPolicy(RetryPolicy.builder().maxAttempts(3).delayMs(0).build())
            .get()
            .build();

        final Response response = client.execute(request);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        wiremock.verify(1, getRequestedFor(urlEqualTo("/ok-retry")));
    }

    @Test
    void retryShouldRetryOnRetryableStatus() throws HttpResponseException {
        wiremock.stubFor(
            get("/retry-status")
                .inScenario("retry-status")
                .whenScenarioStateIs("Started")
                .willReturn(serviceUnavailable())
                .willSetStateTo("attempt2"));

        wiremock.stubFor(
            get("/retry-status")
                .inScenario("retry-status")
                .whenScenarioStateIs("attempt2")
                .willReturn(ok().withHeader("Content-Type", "application/json").withBody("{}")));

        final Request<?> request = Request.builder()
            .url(wiremock.url("/retry-status"))
            .retryPolicy(RetryPolicy.builder().maxAttempts(3).delayMs(0).retryOn(503).build())
            .get()
            .build();

        final Response response = client.execute(request);
        assertEquals(HttpStatus.OK, response.getStatus());
        wiremock.verify(2, getRequestedFor(urlEqualTo("/retry-status")));
    }

    @Test
    void retryShouldThrowAfterExhaustingRetries() {
        wiremock.stubFor(get("/retry-fail").willReturn(serviceUnavailable()));

        final Request<?> request = Request.builder()
            .url(wiremock.url("/retry-fail"))
            .retryPolicy(RetryPolicy.builder().maxAttempts(2).delayMs(0).retryOn(503).build())
            .get()
            .build();

        assertThrows(HttpResponseException.class, () -> client.execute(request));
        wiremock.verify(2, getRequestedFor(urlEqualTo("/retry-fail")));
    }

    @Test
    void retryShouldNotRetryNonRetryableStatus() {
        wiremock.stubFor(get("/retry-notfound").willReturn(notFound()));

        final Request<?> request = Request.builder()
            .url(wiremock.url("/retry-notfound"))
            .retryPolicy(RetryPolicy.builder().maxAttempts(3).delayMs(0).retryOn(503).build())
            .get()
            .build();

        assertThrows(HttpResponseException.class, () -> client.execute(request));
        wiremock.verify(1, getRequestedFor(urlEqualTo("/retry-notfound")));
    }

    @Test
    void asyncShouldSucceedWithRetryPolicy() throws ExecutionException, InterruptedException {
        wiremock.stubFor(
            get("/retry-async")
                .willReturn(ok().withHeader("Content-Type", "application/json").withBody("{}")));

        final Request<?> request = Request.builder()
            .url(wiremock.url("/retry-async"))
            .retryPolicy(RetryPolicy.builder().maxAttempts(2).delayMs(0).build())
            .get()
            .build();

        final CompletableFuture<Response> future = client.async(request);
        final Response response = future.get();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
    }

    @Data
    public static class Result {

        @JsonProperty(value = "message")
        private final String message;

        @JsonCreator
        public Result(@JsonProperty(value = "message") String message) {
            this.message = message;
        }
    }

}
