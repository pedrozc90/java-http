package com.pedrozc90.http.clients;

import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.pedrozc90.http.enums.ContentType;
import com.pedrozc90.http.enums.HttpStatus;
import com.pedrozc90.http.exceptions.HttpResponseException;
import com.pedrozc90.http.objects.Request;
import com.pedrozc90.http.objects.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

public class RetryableHttpClientTest {

    @RegisterExtension
    private static final WireMockExtension wiremock = WireMockExtension.newInstance()
        .options(
            WireMockConfiguration.options()
                .dynamicPort()
                .notifier(new ConsoleNotifier(false))
        )
        .build();

    @Test
    void shouldSucceedOnFirstAttempt() throws HttpResponseException {
        wiremock.stubFor(
            get("/ok")
                .willReturn(ok().withHeader("Content-Type", "application/json").withBody("{}")));

        final RetryPolicy policy = RetryPolicy.builder().maxAttempts(3).delayMs(0).build();
        final HttpClient client = new RetryableHttpClient(new NativeHttpClient(), policy);

        final Request<?> request = Request.builder()
            .url(wiremock.url("/ok"))
            .contentType(ContentType.APPLICATION_JSON)
            .get()
            .build();

        final Response response = client.execute(request);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        // verify only one request was made
        wiremock.verify(1, getRequestedFor(urlEqualTo("/ok")));
    }

    @Test
    void shouldRetryOnRetryableStatus() throws HttpResponseException {
        wiremock.stubFor(
            get("/retry")
                .inScenario("retry")
                .whenScenarioStateIs("Started")
                .willReturn(serviceUnavailable())
                .willSetStateTo("attempt2"));

        wiremock.stubFor(
            get("/retry")
                .inScenario("retry")
                .whenScenarioStateIs("attempt2")
                .willReturn(ok().withHeader("Content-Type", "application/json").withBody("{}")));

        final RetryPolicy policy = RetryPolicy.builder()
            .maxAttempts(3)
            .delayMs(0)
            .retryOn(503)
            .build();
        final HttpClient client = new RetryableHttpClient(new NativeHttpClient(), policy);

        final Request<?> request = Request.builder()
            .url(wiremock.url("/retry"))
            .get()
            .build();

        final Response response = client.execute(request);
        assertEquals(HttpStatus.OK, response.getStatus());
        wiremock.verify(2, getRequestedFor(urlEqualTo("/retry")));
    }

    @Test
    void shouldThrowAfterExhaustingRetries() {
        wiremock.stubFor(get("/fail").willReturn(serviceUnavailable()));

        final RetryPolicy policy = RetryPolicy.builder()
            .maxAttempts(2)
            .delayMs(0)
            .retryOn(503)
            .build();
        final HttpClient client = new RetryableHttpClient(new NativeHttpClient(), policy);

        final Request<?> request = Request.builder()
            .url(wiremock.url("/fail"))
            .get()
            .build();

        assertThrows(HttpResponseException.class, () -> client.execute(request));
        wiremock.verify(2, getRequestedFor(urlEqualTo("/fail")));
    }

    @Test
    void shouldNotRetryNonRetryableStatus() {
        wiremock.stubFor(get("/notfound").willReturn(notFound()));

        final RetryPolicy policy = RetryPolicy.builder()
            .maxAttempts(3)
            .delayMs(0)
            .retryOn(503)
            .build();
        final HttpClient client = new RetryableHttpClient(new NativeHttpClient(), policy);

        final Request<?> request = Request.builder()
            .url(wiremock.url("/notfound"))
            .get()
            .build();

        assertThrows(HttpResponseException.class, () -> client.execute(request));
        wiremock.verify(1, getRequestedFor(urlEqualTo("/notfound")));
    }

    @Test
    void asyncShouldSucceed() throws ExecutionException, InterruptedException {
        wiremock.stubFor(
            get("/async")
                .willReturn(ok().withHeader("Content-Type", "application/json").withBody("{}")));

        final RetryPolicy policy = RetryPolicy.builder().maxAttempts(2).delayMs(0).build();
        final HttpClient client = new RetryableHttpClient(new NativeHttpClient(), policy);

        final Request<?> request = Request.builder()
            .url(wiremock.url("/async"))
            .get()
            .build();

        final CompletableFuture<Response> future = client.async(request);
        final Response response = future.get();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
    }

}
