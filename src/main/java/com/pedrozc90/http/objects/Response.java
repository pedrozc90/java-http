package com.pedrozc90.http.objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pedrozc90.http.enums.HttpStatus;
import com.pedrozc90.http.utils.JsonUtils;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Immutable representation of an HTTP response.
 */
@Data
public class Response {

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
     * The deserialized response payload (may be {@code null} for responses without a body).
     */
    @JsonProperty(value = "payload")
    private final byte[] payload;

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

    public <T> T as(final Class<T> type) throws IOException {
        if (payload == null || type == null) return null;
        return JsonUtils.toObject(payload, type);
    }

    public String asString(final Charset charset) {
        if (payload == null) return null;
        return new String(payload, charset);
    }

    public String asString() {
        return asString(StandardCharsets.UTF_8);
    }

    public File asFile() throws IOException {
        final String contentDisposition = headers.get("Content-Disposition");
        final Path tmp = Files.createTempFile("res", "");
        Files.write(tmp, payload);
        return tmp.toFile();
    }

    public static Response of(final HttpStatus status, final Map<String, String> headers, final byte[] body, final long start) {
        final long elapsed = System.currentTimeMillis() - start;
        return new Response(status, headers, body, elapsed);
    }

    public static Response of(final int status, final Map<String, String> headers, final byte[] body, final long start) {
        return of(HttpStatus.resolve(status), headers, body, start);
    }

}
