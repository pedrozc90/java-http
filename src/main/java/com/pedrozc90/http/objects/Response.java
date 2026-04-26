package com.pedrozc90.http.objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonProperty(value = "status")
    private final HttpStatus status;

    /**
     * HTTP headers returned by the server.
     */
    @JsonProperty(value = "headers")
    private final Map<String, String> headers;

    /**
     * The deserialized response body (may be {@code null} for responses without a body).
     */
    @JsonProperty(value = "body")
    private final T body;

    @JsonIgnore
    private final Class<T> type;

    @JsonProperty(value = "elapsed")
    private final long elapsed;

    // -------------------------------------------------------------------------
    // Convenience status-check methods
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if the response status is in the 2xx Successful range.
     */
    public boolean isSuccessful() {
        return status != null && status.isSuccessful();
    }

    /**
     * Returns {@code true} if the response status is in the 4xx Client Error range.
     */
    public boolean isClientError() {
        return status != null && status.isClientError();
    }

    /**
     * Returns {@code true} if the response status is in the 5xx Server Error range.
     */
    public boolean isServerError() {
        return status != null && status.isServerError();
    }

    /**
     * Returns {@code true} if the response status represents any error (4xx or 5xx).
     */
    public boolean isError() {
        return status != null && status.isError();
    }

    public static <T> Response<T> of(final int status, final Map<String, String> headers, final T body, final Class<T> type, final long start) {
        final long elapsed = System.currentTimeMillis() - start;
        return new Response<>(HttpStatus.resolve(status), headers, body, type, elapsed);
    }

}
