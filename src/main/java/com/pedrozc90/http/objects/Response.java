package com.pedrozc90.http.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.pedrozc90.http.enums.HttpHeader;
import com.pedrozc90.http.enums.HttpStatus;
import com.pedrozc90.http.utils.HeaderUtils;
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
@JsonSerialize(using = Response.Serializer.class)
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

    @JsonProperty(value = "length")
    private final long length;

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
        this.length = (payload != null) ? payload.length : 0;
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
        String prefix = "res";
        String suffix = "";

        final String contentDisposition = headers != null ? headers.get(HttpHeader.CONTENT_DISPOSITION) : null;
        if (contentDisposition != null && !contentDisposition.isBlank()) {
            final String filename = HeaderUtils.getFilenameFromContentDisposition(contentDisposition);
            if (filename != null) {
                final int dot = filename.lastIndexOf('.');
                prefix = dot > 0 ? filename.substring(0, dot) : filename;
                suffix = dot > 0 ? filename.substring(dot) : "";
            }
        }

        final Path tmp = Files.createTempFile(prefix, suffix);
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

    // -------------------------------------------------------------------------
    // Jackson Serializer — smart payload rendering
    // -------------------------------------------------------------------------

    /**
     * Custom Jackson serializer for {@link Response}.
     * <p>
     * The {@code payload} field is rendered based on the response {@code Content-Type}:
     * <ul>
     *   <li>{@code application/json} — payload is pretty-printed as a JSON tree</li>
     *   <li>{@code text/*} — payload is rendered as a UTF-8 string</li>
     *   <li>anything else — a placeholder {@code <binary data, N bytes>} is emitted</li>
     * </ul>
     */
    public static class Serializer extends StdSerializer<Response> {

        public Serializer() {
            super(Response.class);
        }

        @Override
        public void serialize(final Response response, final JsonGenerator gen, final SerializerProvider provider) throws IOException {
            if (response == null) {
                gen.writeNull();
            } else {
                gen.writeStartObject();
                provider.defaultSerializeField("status", response.getStatus(), gen);
                provider.defaultSerializeField("headers", response.getHeaders(), gen);
                gen.writeNumberField("elapsed", response.getElapsed());
                serializePayload(response, gen);
                gen.writeEndObject();
            }
        }

        private static void serializePayload(final Response response, final JsonGenerator gen) throws IOException {
            gen.writeFieldName("payload");

            final byte[] payload = response.getPayload();
            if (payload == null || payload.length == 0) {
                gen.writeNull();
                return;
            }

            final String contentType = findHeader(response.getHeaders(), HttpHeader.CONTENT_TYPE);

            if (contentType != null && contentType.contains("application/json")) {
                try {
                    final JsonNode node = JsonUtils.getMapper().readTree(payload);
                    gen.writeTree(node);
                } catch (JsonProcessingException e) {
                    gen.writeString(new String(payload, StandardCharsets.UTF_8));
                }
            } else if (contentType != null && contentType.startsWith("text/")) {
                gen.writeString(new String(payload, StandardCharsets.UTF_8));
            } else {
                gen.writeString("<binary data, " + payload.length + " bytes>");
            }
        }

        private static String findHeader(final Map<String, String> headers, final String name) {
            if (headers == null) return null;
            return headers.entrySet().stream()
                .filter(e -> name.equalsIgnoreCase(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
        }
    }

}
