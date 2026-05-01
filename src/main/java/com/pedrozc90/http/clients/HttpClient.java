package com.pedrozc90.http.clients;

import com.pedrozc90.http.exceptions.HttpResponseException;
import com.pedrozc90.http.objects.Request;
import com.pedrozc90.http.objects.Response;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Abstraction for executing HTTP requests.
 *
 * <p>Implementations may wrap any underlying HTTP client library
 * (e.g. Apache HttpClient, Play WS, OkHttp).
 */
public interface HttpClient {

    /**
     * Executes the given HTTP request and deserializes the response body to {@code responseType}.
     *
     * @param request      the request to execute; must not be {@code null}
     * @return the HTTP response with the deserialized body
     * @throws HttpResponseException if the request could not be executed
     */
    Response execute(final Request<?> request) throws HttpResponseException;

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
