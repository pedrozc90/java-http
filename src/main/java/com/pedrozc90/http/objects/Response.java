package com.pedrozc90.http.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.pedrozc90.http.enums.HttpHeader;
import com.pedrozc90.http.enums.HttpStatus;
import com.pedrozc90.http.utils.FileUtils;
import com.pedrozc90.http.utils.HeaderUtils;
import com.pedrozc90.http.utils.JsonUtils;
import com.pedrozc90.http.utils.StringUtils;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

/**
 * Immutable representation of an HTTP response.
 */
@Data
@JsonSerialize(using = ResponseSerializer.class)
@JsonDeserialize(using = ResponseDeserializer.class)
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
     * The raw response payload (may be {@code null} for responses without a body).
     */
    @JsonProperty(value = "payload")
    private final byte[] payload;

    @JsonProperty(value = "elapsed")
    private final long elapsed;

    @JsonCreator
    public Response(
        @JsonProperty(value = "status") final HttpStatus status,
        @JsonProperty(value = "headers") final Map<String, String> headers,
        @JsonProperty(value = "payload") final byte[] payload,
        @JsonProperty(value = "elapsed") final long elapsed
    ) {
        this.status = status;
        this.headers = headers;
        this.payload = payload;
        this.elapsed = elapsed;
    }

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

    // -------------------------------------------------------------------------
    // Payload helpers
    // -------------------------------------------------------------------------

    /**
     * Returns the number of bytes in the payload, or {@code 0} when there is no payload.
     */
    public long getLength() {
        return payload != null ? payload.length : 0;
    }

    /**
     * Returns {@code true} when the {@code Content-Type} header indicates JSON content.
     */
    public boolean isJson() {
        final String contentType = HeaderUtils.findHeader(headers, HttpHeader.CONTENT_TYPE);
        return contentType != null && contentType.contains("application/json");
    }

    /**
     * Returns {@code true} when the response carries a file attachment, indicated by the
     * presence of a {@code Content-Disposition} header.
     */
    public boolean isFile() {
        return StringUtils.isNotBlank(HeaderUtils.findHeader(headers, HttpHeader.CONTENT_DISPOSITION));
    }

    /**
     * Returns the charset declared in the {@code Content-Type} header
     * (e.g. {@code text/html; charset=ISO-8859-1}), falling back to {@link StandardCharsets#UTF_8}.
     */
    public Charset getCharset() {
        return HeaderUtils.parseCharset(HeaderUtils.findHeader(headers, HttpHeader.CONTENT_TYPE));
    }

    // -------------------------------------------------------------------------
    // Payload conversion
    // -------------------------------------------------------------------------

    public <T> T as(final Class<T> type) throws IOException {
        if (payload == null || type == null) return null;
        return JsonUtils.toObject(payload, type);
    }

    public String asString(final Charset charset) {
        if (payload == null) return null;
        return new String(payload, charset);
    }

    /**
     * Decodes the payload to a {@code String} using the charset from the
     * {@code Content-Type} header, defaulting to UTF-8.
     */
    public String asString() {
        return asString(getCharset());
    }

    /**
     * Saves the response payload to disk and returns an {@link HttpFile} with the
     * original file metadata.
     *
     * <p>The file is stored at {@code /tmp/<filename>} when a filename can be parsed
     * from the {@code Content-Disposition} header; otherwise a unique temp file is
     * created via {@link Files#createTempFile}.
     */
    public HttpFile asFile() throws IOException {
        final String contentDisposition = HeaderUtils.findHeader(headers, HttpHeader.CONTENT_DISPOSITION);
        final String contentType = HeaderUtils.findHeader(headers, HttpHeader.CONTENT_TYPE);

        String filename = null;
        if (StringUtils.isNotBlank(contentDisposition)) {
            filename = HeaderUtils.getFilenameFromContentDisposition(contentDisposition);
        }

        final File tmp = FileUtils.createTempFile(filename);

        Files.write(tmp.toPath(), payload);

        return new HttpFile(tmp, filename, contentType, payload != null ? payload.length : 0);
    }

    // -------------------------------------------------------------------------
    // Factory methods
    // -------------------------------------------------------------------------

    public static Response of(final HttpStatus status, final Map<String, String> headers, final byte[] body, final long start) {
        final long elapsed = System.currentTimeMillis() - start;
        return new Response(status, headers, body, elapsed);
    }

    public static Response of(final int status, final Map<String, String> headers, final byte[] body, final long start) {
        return of(HttpStatus.resolve(status), headers, body, start);
    }

}

