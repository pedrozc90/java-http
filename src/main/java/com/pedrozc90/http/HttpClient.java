package com.pedrozc90.http;

import com.pedrozc90.http.exceptions.HttpClientException;
import com.pedrozc90.http.models.Request;
import com.pedrozc90.http.models.Response;

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
     * @throws HttpClientException if the request could not be executed
     */
    <T> Response<T> execute(Request<?> request, Class<T> responseType) throws HttpClientException;

    /**
     * Executes the given HTTP request and returns the raw response body as a {@link String}.
     *
     * @param request the request to execute; must not be {@code null}
     * @return the HTTP response with a {@link String} body
     * @throws HttpClientException if the request could not be executed
     */
    Response<String> execute(Request<?> request) throws HttpClientException;
}
