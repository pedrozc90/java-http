package com.pedrozc90.http.clients;

import com.pedrozc90.http.exceptions.HttpResponseException;
import com.pedrozc90.http.objects.Request;
import com.pedrozc90.http.objects.Response;

import java.util.concurrent.CompletableFuture;

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
     * @param <T>          the expected response body type
     * @param request      the request to execute; must not be {@code null}
     * @param responseType the class of the expected response body; must not be {@code null}
     * @return the HTTP response with the deserialized body
     * @throws HttpResponseException if the request could not be executed
     */
    <T> Response<T> execute(final Request<?> request, final Class<T> responseType) throws HttpResponseException;

    /**
     * Executes the given HTTP request and returns the raw response body as a {@link String}.
     *
     * @param request the request to execute; must not be {@code null}
     * @return the HTTP response with a {@link String} body
     * @throws HttpResponseException if the request could not be executed
     */
    Response<String> execute(final Request<?> request) throws HttpResponseException;

    <T> CompletableFuture<Response<T>> async(final Request<T> request, final Class<T> responseType);
}
