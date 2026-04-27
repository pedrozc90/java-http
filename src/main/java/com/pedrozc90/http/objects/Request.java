package com.pedrozc90.http.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pedrozc90.http.enums.ContentType;
import com.pedrozc90.http.enums.HttpHeader;
import com.pedrozc90.http.enums.HttpMethod;
import lombok.Data;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
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
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    /* --- Builder --- */
    public interface UrlStep<T> {
        QueryStep<T> url(final String url);

        QueryStep<T> url(final String fmt, final Object... args);
    }

    public interface QueryStep<T> extends HeaderStep<T> {
        QueryStep<T> query(final String key, final String value);

        HeaderStep<T> header(final String key, final String value);
    }

    public interface HeaderStep<T> extends MethodStep<T> {
        HeaderStep<T> header(final String key, final String value);

        HeaderStep<T> authorization(final String value);

        HeaderStep<T> bearer(final String token);

        HeaderStep<T> contentType(final String value);

        HeaderStep<T> contentType(final ContentType value);

        HeaderStep<T> timeout(final Integer value);
    }

    public interface MethodStep<T> {
        BodyStep<T> get();

        BodyStep<T> head();

        BodyStep<T> post();

        BodyStep<T> put();

        BodyStep<T> patch();

        BodyStep<T> delete();

        BodyStep<T> options();

        BodyStep<T> trace();
    }

    public interface BodyStep<T> extends BuildStep<T> {
        BodyStep<T> body();

        BodyStep<T> body(final T body, final Class<T> bodyType);
    }

    public interface BuildStep<T> {
        Request<T> build();

        Request<T> build(final Charset charset);
    }

    @Data
    public static class Builder<T> implements UrlStep<T>, QueryStep<T>, HeaderStep<T>, BodyStep<T>, BuildStep<T> {

        private String url;
        private final Map<String, String> query = new LinkedHashMap<>();
        private HttpMethod method;
        private final Map<String, String> headers = new LinkedHashMap<>();
        private T body;
        private Class<?> bodyType;
        private Integer timeout;

        /* --- URL --- */
        @Override
        public QueryStep<T> url(final String url) {
            this.url = url;
            return this;
        }

        @Override
        public QueryStep<T> url(final String fmt, final Object... args) {
            final String url = String.format(fmt, args);
            return url(url);
        }

        /* --- Query --- */
        @Override
        public QueryStep<T> query(final String key, final String value) {
            query.put(key, value);
            return this;
        }

        /* --- Headers --- */
        @Override
        public HeaderStep<T> header(final String key, final String value) {
            headers.put(key, value);
            return this;
        }

        @Override
        public HeaderStep<T> authorization(final String value) {
            return header(HttpHeader.AUTHORIZATION, value);
        }

        @Override
        public HeaderStep<T> bearer(final String token) {
            return authorization("Bearer " + token);
        }

        @Override
        public HeaderStep<T> contentType(final String value) {
            return header(HttpHeader.CONTENT_TYPE, value);
        }

        @Override
        public HeaderStep<T> contentType(final ContentType value) {
            return contentType(value.getValue());
        }

        @Override
        public HeaderStep<T> timeout(Integer value) {
            this.timeout = value;
            return this;
        }

        /* --- Methods --- */
        private BodyStep<T> method(final HttpMethod method) {
            this.method = method;
            return this;
        }

        @Override
        public BodyStep<T> get() {
            return method(HttpMethod.GET);
        }

        @Override
        public BodyStep<T> head() {
            return method(HttpMethod.HEAD);
        }

        @Override
        public BodyStep<T> post() {
            return method(HttpMethod.POST);
        }

        @Override
        public BodyStep<T> put() {
            return method(HttpMethod.PUT);
        }

        @Override
        public BodyStep<T> patch() {
            return method(HttpMethod.PATCH);
        }

        @Override
        public BodyStep<T> delete() {
            return method(HttpMethod.DELETE);
        }

        @Override
        public BodyStep<T> options() {
            return method(HttpMethod.OPTIONS);
        }

        @Override
        public BodyStep<T> trace() {
            return method(HttpMethod.TRACE);
        }

        /* --- Body --- */
        @Override
        public BodyStep<T> body(final T body, final Class<T> bodyType) {
            this.body = body;
            this.bodyType = bodyType;
            return this;
        }

        @Override
        public BodyStep<T> body() {
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Request<T> build(final Charset charset) {
            if (url == null) throw new IllegalStateException("URL must be defined");
            if (method == null) throw new IllegalStateException("HTTP method must be defined");
            return new Request<>(url, method, headers, body, timeout, charset, (Class<T>) bodyType);
        }

        @Override
        public Request<T> build() {
            return build(null);
        }
    }


}
