package com.pedrozc90.http.objects;

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
    private final Integer timeout;

    @JsonIgnore
    private final Class<T> type;

    @JsonIgnore
    private final Charset charset = StandardCharsets.UTF_8;

    /* --- Helpers --- */
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    /* --- Builder --- */
    public interface UrlStep<T> {
        QueryStep<T> url(final String url);
    }

    public interface QueryStep<T> {
        QueryStep<T> query(final String key, final String value);

        HeaderStep<T> header(final String key, final String value);

        BodyStep<T> get();

        BodyStep<T> post();

        BodyStep<T> put();

        BodyStep<T> delete();
    }

    public interface HeaderStep<T> {
        HeaderStep<T> header(final String key, final String value);

        HeaderStep<T> authorization(final String value);

        HeaderStep<T> bearer(final String token);

        HeaderStep<T> contentType(final String value);

        HeaderStep<T> contentType(final ContentType value);

        BodyStep<T> get();

        BodyStep<T> post();

        BodyStep<T> put();

        BodyStep<T> delete();
    }

    public interface BodyStep<T> extends BuildStep<T> {
        BodyStep<T> body();

        BodyStep<T> body(final T body, final Class<T> bodyType);
    }

    public interface BuildStep<T> {
        Request<T> build();
    }

    @Data
    public static class Builder<T> implements UrlStep<T>, QueryStep<T>, HeaderStep<T>, BodyStep<T>, BuildStep<T> {

        private String url;
        //        private final List<String> paths = new ArrayList<>();
        private final Map<String, String> query = new LinkedHashMap<>();
        private HttpMethod method;
        private final Map<String, String> headers = new LinkedHashMap<>();
        private T body;
        private Class<?> bodyType;
        private int timeout = 10_000;

        /* --- URL --- */
        @Override
        public QueryStep<T> url(final String url) {
            this.url = url;
            return this;
        }

        /* --- QUERY --- */
        @Override
        public QueryStep<T> query(final String key, final String value) {
            query.put(key, value);
            return this;
        }

        /* --- HEADER --- */
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

        private BodyStep<T> method(final HttpMethod method) {
            this.method = method;
            return this;
        }

        @Override
        public BodyStep<T> get() {
            return method(HttpMethod.GET);
        }

        @Override
        public BodyStep<T> post() {
            return method(HttpMethod.POST);
        }

        @Override
        public BodyStep<T> put() {
            return method(HttpMethod.POST);
        }

        @Override
        public BodyStep<T> delete() {
            return method(HttpMethod.DELETE);
        }

        /* --- BODY --- */
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
        public Request<T> build() {
            if (url == null) throw new IllegalStateException("URL must be defined");

            if (method == null) throw new IllegalStateException("HTTP method must be defined");

            return new Request<>(url, method, headers, body, timeout, (Class<T>) bodyType);
        }

    }


}
