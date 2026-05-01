package com.pedrozc90.http.objects;

import com.pedrozc90.http.clients.RetryPolicy;
import com.pedrozc90.http.enums.ContentType;
import com.pedrozc90.http.enums.HttpHeader;
import com.pedrozc90.http.enums.HttpMethod;
import com.pedrozc90.http.utils.StringUtils;
import lombok.Data;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Step-builder for constructing {@link Request} instances.
 *
 * <p>Obtain a builder via {@link Request#builder()}.
 */
public class RequestBuilder {

    private RequestBuilder() {
    }

    /* --- Step interfaces --- */
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

        HeaderStep<T> accept(final String value);

        HeaderStep<T> accept(final ContentType value);

        HeaderStep<T> timeout(final Integer value);

        HeaderStep<T> retryPolicy(final RetryPolicy policy);
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

    /* --- Builder --- */
    @Data
    public static class Builder<T> implements UrlStep<T>, QueryStep<T>, HeaderStep<T>, BodyStep<T>, BuildStep<T> {

        private String url;
        private final Map<String, String> query = new LinkedHashMap<>();
        private HttpMethod method;
        private final Map<String, String> headers = new LinkedHashMap<>();
        private T body;
        private Class<?> bodyType;
        private Integer timeout;
        private RetryPolicy retryPolicy;

        /* --- URL --- */
        @Override
        public QueryStep<T> url(final String url) {
            this.url = url;
            return this;
        }

        @Override
        public QueryStep<T> url(final String fmt, final Object... args) {
            return url(String.format(fmt, args));
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
            return contentType(value.value());
        }

        @Override
        public HeaderStep<T> accept(final String value) {
            return header(HttpHeader.ACCEPT, value);
        }

        @Override
        public HeaderStep<T> accept(final ContentType value) {
            return accept(value.value());
        }

        @Override
        public HeaderStep<T> timeout(final Integer value) {
            this.timeout = value;
            return this;
        }

        @Override
        public HeaderStep<T> retryPolicy(final RetryPolicy policy) {
            this.retryPolicy = policy;
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

        /* --- Build --- */
        @Override
        @SuppressWarnings("unchecked")
        public Request<T> build(final Charset charset) {
            if (StringUtils.isBlank(url)) throw new IllegalStateException("URL must be defined");
            if (method == null) throw new IllegalStateException("HTTP method must be defined");

            final String resolvedUrl;
            if (!query.isEmpty()) {
                final StringBuilder sb = new StringBuilder(url);
                sb.append('?');
                boolean first = true;
                try {
                    for (final Map.Entry<String, String> entry : query.entrySet()) {
                        if (!first) sb.append('&');
                        sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name()));
                        sb.append('=');
                        sb.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name()));
                        first = false;
                    }
                } catch (UnsupportedEncodingException e) {
                    throw new IllegalStateException("UTF-8 encoding not supported", e);
                }
                resolvedUrl = sb.toString();
            } else {
                resolvedUrl = url;
            }

            return new Request<>(resolvedUrl, method, headers, body, timeout, charset, (Class<T>) bodyType, retryPolicy);
        }

        @Override
        public Request<T> build() {
            return build(null);
        }
    }

}
