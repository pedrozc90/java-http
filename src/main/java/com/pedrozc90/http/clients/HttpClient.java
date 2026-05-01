package com.pedrozc90.http.clients;

import com.pedrozc90.http.exceptions.HttpResponseException;
import com.pedrozc90.http.objects.Request;
import com.pedrozc90.http.objects.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Abstraction for executing HTTP requests.
 *
 * <p>Implementations may wrap any underlying HTTP client library
 * (e.g. Apache HttpClient, Play WS, OkHttp).
 *
 * <p>Implementations only need to provide {@link #executeOnce(Request)}.
 * Retry logic (when a {@link RetryPolicy} is set on the request) is handled
 * automatically by the {@link #execute(Request)} default method in this interface
 * and does not need to be repeated in every implementation.
 */
public interface HttpClient {

    Logger log = LoggerFactory.getLogger(HttpClient.class);

    /**
     * Performs a single HTTP attempt with no retry logic.
     * Implementations must provide this method.
     *
     * @param request the request to execute; must not be {@code null}
     * @return the HTTP response
     * @throws HttpResponseException if the request fails
     */
    Response executeOnce(final Request<?> request) throws HttpResponseException;

    /**
     * Executes the given HTTP request, automatically retrying according to the
     * {@link RetryPolicy} attached to the request (if any).
     *
     * <p>When no {@link RetryPolicy} is set on the request this is equivalent
     * to a single call to {@link #executeOnce(Request)}.
     *
     * @param request the request to execute; must not be {@code null}
     * @return the HTTP response
     * @throws HttpResponseException if all attempts fail
     */
    default Response execute(final Request<?> request) throws HttpResponseException {
        final RetryPolicy policy = request.getRetryPolicy();

        if (policy == null) {
            return executeOnce(request);
        }

        // executeOnce throws HttpResponseException for every non-2xx status, so all
        // retry decisions are made in the catch block; the try block only needs to
        // return on success.
        HttpResponseException lastException = null;

        for (int attempt = 1; attempt <= policy.getMaxAttempts(); attempt++) {
            try {
                return executeOnce(request);
            } catch (HttpResponseException e) {
                lastException = e;

                final Response response = e.getResponse();

                final boolean isRetryableStatus = response != null && policy.isRetryable(response.getStatus());
                final boolean isRetryableException = e.getCause() != null && policy.isRetryOnException();

                if ((isRetryableStatus || isRetryableException) && attempt < policy.getMaxAttempts()) {
                    log.warn("Retrying request {} {} after error (attempt {}/{}): {}", request.getMethod(), request.getUrl(), attempt, policy.getMaxAttempts(), e.getMessage());
                    RetryPolicy.sleep(policy.getDelayMs());
                } else {
                    throw e;
                }
            }
        }

        // Unreachable when maxAttempts >= 1 (enforced by RetryPolicy.Builder): the
        // last iteration always hits the else-throw above.  Guard defensively so the
        // compiler is satisfied and a broken policy can't produce a NullPointerException.
        throw lastException != null ? lastException : new HttpResponseException("Retry loop exhausted without result", request, null);
    }

    /**
     * Executes the given HTTP request asynchronously using the provided executor.
     *
     * <p>Any exception during execution is wrapped in a {@link java.util.concurrent.CompletionException}.
     *
     * @param request  the request to execute; must not be {@code null}
     * @param executor the executor used to run the async task
     * @return a future that completes with the HTTP response
     */
    CompletableFuture<Response> async(final Request<?> request, final Executor executor);

    /**
     * Executes the given HTTP request asynchronously using the
     * {@link HttpClientExecutor#defaultExecutor() shared default executor}.
     *
     * @param request the request to execute; must not be {@code null}
     * @return a future that completes with the HTTP response
     */
    default CompletableFuture<Response> async(final Request<?> request) {
        return async(request, HttpClientExecutor.defaultExecutor());
    }
}
