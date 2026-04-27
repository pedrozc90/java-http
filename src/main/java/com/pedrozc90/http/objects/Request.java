package com.pedrozc90.http.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pedrozc90.http.enums.HttpMethod;
import lombok.Data;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
    @JsonProperty(value = "url")
    private final String url;

    /**
     * The HTTP method (GET, POST, PUT, etc.).
     */
    @JsonProperty(value = "method")
    private final HttpMethod method;

    /**
     * HTTP headers to send with the request.
     */
    @JsonProperty(value = "headers")
    private final Map<String, String> headers;

    /**
     * The request body (may be {@code null} for methods without a body, e.g. GET).
     */
    @JsonProperty(value = "body")
    private final T body;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty(value = "timeout")
    private final int timeout;

    @JsonProperty(value = "charset")
    private final Charset charset;

    @JsonIgnore
    private final Class<T> type;

    public Request(
        @JsonProperty(value = "url") final String url,
        @JsonProperty(value = "method") final HttpMethod method,
        @JsonProperty(value = "headers") final Map<String, String> headers,
        @JsonProperty(value = "body") final T body,
        @JsonProperty(value = "timeout") final Integer timeout,
        @JsonProperty(value = "charset") final Charset charset,
        final Class<T> type
    ) {
        this.url = url;
        this.method = method;
        this.headers = headers;
        this.body = body;
        this.timeout = (timeout != null) ? Math.max(timeout, 0) : 5_000;
        this.charset = (charset != null) ? charset : StandardCharsets.UTF_8;
        this.type = type;
    }

    @JsonCreator
    public Request(
        @JsonProperty(value = "url") final String url,
        @JsonProperty(value = "method") final HttpMethod method,
        @JsonProperty(value = "headers") final Map<String, String> headers,
        @JsonProperty(value = "body") final T body,
        @JsonProperty(value = "timeout") final Integer timeout
    ) {
        this(url, method, headers, body, timeout, null, null);
    }

    /* --- Helpers --- */
    public static <T> RequestBuilder.Builder<T> builder() {
        return new RequestBuilder.Builder<>();
    }

}

