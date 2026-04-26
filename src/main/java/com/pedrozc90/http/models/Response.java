package com.pedrozc90.http.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pedrozc90.http.enums.HttpStatus;
import lombok.Data;

import java.util.Map;

/**
 * Immutable representation of an HTTP response.
 *
 * @param <T> the type of the response body
 */
@Data
public class Response<T> {

    /**
     * The HTTP status of the response.
     */
    @JsonProperty("status")
    private final HttpStatus status;

    /**
     * HTTP headers returned by the server.
     */
    @JsonProperty("headers")
    private final Map<String, String> headers;

    /**
     * The deserialized response body (may be {@code null} for responses without a body).
     */
    @JsonProperty("body")
    private final T body;

    // -------------------------------------------------------------------------
    // Convenience status-check methods
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if the response status is in the 2xx Successful range.
     */
    public boolean isSuccessful() {
        return status != null && status.is2xxSuccessful();
    }

    /**
     * Returns {@code true} if the response status is in the 4xx Client Error range.
     */
    public boolean isClientError() {
        return status != null && status.is4xxClientError();
    }

    /**
     * Returns {@code true} if the response status is in the 5xx Server Error range.
     */
    public boolean isServerError() {
        return status != null && status.is5xxServerError();
    }

    /**
     * Returns {@code true} if the response status represents any error (4xx or 5xx).
     */
    public boolean isError() {
        return status != null && status.isError();
    }

    /**
     * Returns the numeric HTTP status code, or {@code -1} if the status is {@code null}.
     */
    public int getStatusCode() {
        return status != null ? status.value() : -1;
    }
}
