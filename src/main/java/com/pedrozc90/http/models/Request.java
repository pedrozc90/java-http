package com.pedrozc90.http.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pedrozc90.http.enums.HttpMethod;
import lombok.Data;

import java.util.Map;

/**
 * Immutable representation of an HTTP request.
 *
 * @param <T> the type of the request body
 */
@Data
public class Request<T> {

    /**
     * The target URL of the request.
     */
    @JsonProperty("url")
    private final String url;

    /**
     * The HTTP method (GET, POST, PUT, etc.).
     */
    @JsonProperty("method")
    private final HttpMethod method;

    /**
     * HTTP headers to send with the request.
     */
    @JsonProperty("headers")
    private final Map<String, String> headers;

    /**
     * The request body (may be {@code null} for methods without a body, e.g. GET).
     */
    @JsonProperty("body")
    private final T body;
}
