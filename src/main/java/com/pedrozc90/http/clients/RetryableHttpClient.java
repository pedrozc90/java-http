package com.pedrozc90.http.clients;

import com.pedrozc90.http.exceptions.HttpResponseException;
import com.pedrozc90.http.objects.Request;
import com.pedrozc90.http.objects.Response;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

/**
 * {@link HttpClient} decorator that transparently retries failed requests
 * according to a {@link RetryPolicy}.
 *
 * <p>A retry is performed when:
 * <ul>
 *   <li>the response status code is listed in
 *       {@link RetryPolicy#getRetryableStatusCodes()}, <em>or</em></li>
 *   <li>the delegate throws an {@link HttpResponseException} caused by a
 *       network-level error and {@link RetryPolicy#isRetryOnException()} is {@code true}.</li>
 * </ul>
 *
 * <p>Example:
 * <pre>{@code
 * RetryPolicy policy = RetryPolicy.builder()
 *     .maxAttempts(3)
 *     .delayMs(500)
 *     .retryOn(429, 503)
 *     .build();
 *
 * HttpClient client = new RetryableHttpClient(new NativeHttpClient(), policy);
 * Response response = client.execute(request);
 * }</pre>
 */
public class RetryableHttpClient implements HttpClient {

    private static final Logger log = Logger.getLogger(RetryableHttpClient.class.getName());

    private final HttpClient delegate;
    private final RetryPolicy policy;
    private final Executor executor;

    /**
     * Creates a retryable client using the provided delegate and policy,
     * and the {@link HttpClientExecutor#defaultExecutor() shared default executor}
     * for {@link #async(Request)} calls.
     *
     * @param delegate the underlying HTTP client
     * @param policy   retry configuration
     */
    public RetryableHttpClient(final HttpClient delegate, final RetryPolicy policy) {
        this(delegate, policy, HttpClientExecutor.defaultExecutor());
    }

    /**
     * Creates a retryable client using the provided delegate, policy, and executor.
     *
     * @param delegate the underlying HTTP client
     * @param policy   retry configuration
     * @param executor executor for {@link #async(Request)} calls
     */
    public RetryableHttpClient(final HttpClient delegate, final RetryPolicy policy, final Executor executor) {
        if (delegate == null) throw new IllegalArgumentException("delegate must not be null");
        if (policy == null) throw new IllegalArgumentException("policy must not be null");
        if (executor == null) throw new IllegalArgumentException("executor must not be null");
        this.delegate = delegate;
        this.policy = policy;
        this.executor = executor;
    }

    @Override
    public Response execute(final Request<?> request) throws HttpResponseException {
        HttpResponseException lastException = null;

        for (int attempt = 1; attempt <= policy.getMaxAttempts(); attempt++) {
            try {
                final Response response = delegate.execute(request);

                if (response.getStatus() != null
                    && policy.isRetryable(response.getStatus().value())
                    && attempt < policy.getMaxAttempts()) {
                    log.warning(String.format(
                        "Retrying request %s %s after retryable status %s (attempt %d/%d)",
                        request.getMethod(), request.getUrl(),
                        response.getStatus(), attempt, policy.getMaxAttempts()));
                    sleep(policy.getDelayMs());
                    continue;
                }

                return response;
            } catch (HttpResponseException e) {
                lastException = e;

                final boolean isRetryableStatus = e.getResponse() != null
                    && e.getResponse().getStatus() != null
                    && policy.isRetryable(e.getResponse().getStatus().value());
                final boolean isRetryableException = e.getCause() != null && policy.isRetryOnException();

                if ((isRetryableStatus || isRetryableException) && attempt < policy.getMaxAttempts()) {
                    log.warning(String.format(
                        "Retrying request %s %s after error (attempt %d/%d): %s",
                        request.getMethod(), request.getUrl(),
                        attempt, policy.getMaxAttempts(), e.getMessage()));
                    sleep(policy.getDelayMs());
                } else {
                    throw e;
                }
            }
        }

        throw lastException;
    }

    @Override
    public CompletableFuture<Response> async(final Request<?> request, final Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute(request);
            } catch (HttpResponseException e) {
                throw e.toCompletionException();
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Response> async(final Request<?> request) {
        return async(request, this.executor);
    }

    private static void sleep(final long millis) {
        if (millis <= 0) return;
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
