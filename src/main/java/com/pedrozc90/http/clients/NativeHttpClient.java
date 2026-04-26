package com.pedrozc90.http.clients;

import com.pedrozc90.http.exceptions.HttpResponseException;
import com.pedrozc90.http.objects.Request;
import com.pedrozc90.http.objects.Response;
import com.pedrozc90.http.utils.JsonUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class NativeHttpClient implements HttpClient {

    @Override
    public <T> Response<T> execute(final Request<?> request, final Class<T> responseType) throws HttpResponseException {
        final long start = System.currentTimeMillis();

        int status = -1;
        String message = null;
        String content = null;
        Map<String, String> responseHeaders = null;

        try {
            // Create a Url object from the url.
            // final URL url = request.uri.toURL();
            final URL url = new URL(request.getUrl());

            // Open a connection to the URL.
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Connection", "keep-alive");
            connection.setRequestMethod(request.getMethod().name());
            if (request.getType() != null) {
                connection.setConnectTimeout(request.getTimeout());
            }

            // Set the request headers.
            request.getHeaders().forEach((key, value) -> connection.setRequestProperty(key, value));

            // Send the request body.
            final Object body = request.getBody();

            if (body != null) {
                final String bodyStr = (body instanceof String)
                    ? (String) body
                    : JsonUtils.toString(body);

                if (bodyStr != null && !bodyStr.isBlank()) {
                    connection.setDoOutput(true);
                    try (OutputStream os = connection.getOutputStream()) {
                        byte[] bytes = bodyStr.getBytes(request.getCharset());
                        os.write(bytes, 0, bytes.length);
                    }
                }
            }

            status = connection.getResponseCode();
            message = connection.getResponseMessage();

            responseHeaders = getResponseHeaders(connection);

            final InputStream is = (status >= 200 && status < 300)
                ? connection.getInputStream()
                : connection.getErrorStream();

            try {
                if (is != null) {
                    try (Scanner scanner = new Scanner(is, request.getCharset())) {
                        if (scanner.hasNext()) {
                            content = scanner.useDelimiter("\\A").next();
                        }
                    }
                }
            } finally {
                connection.disconnect();
            }

            final long elapsed = System.currentTimeMillis() - start;

            final T object = JsonUtils.toObject(content, responseType);

            final Response<T> response = Response.of(status, responseHeaders, object, responseType, elapsed);

            if (status < 200 || status >= 300) {
                throw new HttpResponseException(message, request, response);
            }

            return response;
        } catch (IOException e) {
            final Response<String> response = Response.of(status, responseHeaders, content, String.class, start);
            throw new HttpResponseException(e, request, response);
        }
    }

    @Override
    public Response<String> execute(final Request<?> request) throws HttpResponseException {
        return execute(request, String.class);
    }

    @Override
    public <T> CompletableFuture<Response<T>> async(final Request<T> request, final Class<T> responseType) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute(request, responseType);
            } catch (HttpResponseException e) {
                // TODO: how do we properly handle this?
                throw e.toCompletionException();
            }
        });
    }

    /* --- Helpers --- */
    private static Map<String, String> getResponseHeaders(final HttpURLConnection connection) {
        final Map<String, String> out = new HashMap<>();

        final Map<String, List<String>> headers = connection.getHeaderFields();
        if (headers == null || headers.isEmpty()) return out;

        headers.forEach((key, value) -> {
            out.put(key, String.join("; ", value));
        });

        return out;
    }

}
