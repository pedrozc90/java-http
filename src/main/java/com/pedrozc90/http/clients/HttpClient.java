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
 * (e.g. Apache HttpClient, Play WS, OkHttp, Java 11 {@code HttpClient}).
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


    CompletableFuture<Response> async(final Request<?> request, final Executor executor) throws HttpResponseException;
}
