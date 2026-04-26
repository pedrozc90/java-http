package com.pedrozc90.http.clients;

import com.pedrozc90.http.enums.HttpStatus;
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
import java.util.concurrent.CompletableFuture;

public class NativeHttpClient implements HttpClient {

    @Override
    public Response execute(final Request<?> request) throws HttpResponseException {
        final long start = System.currentTimeMillis();

        int status = -1;
        String message = null;
        byte[] content = null;
        Map<String, String> headers = null;

        try {
            // Create a Url object from the url.
            // final URL url = request.uri.toURL();
            final URL url = new URL(request.getUrl());

            // Open a connection to the URL.
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Connection", "keep-alive");
            connection.setRequestMethod(request.getMethod().name());

            final Integer timeout = request.getTimeout();
            if (timeout != null) {
                connection.setConnectTimeout(timeout);
                connection.setReadTimeout(timeout);
            }

            // Set the request headers.
            request.getHeaders().forEach((key, value) -> connection.setRequestProperty(key, value));

            // Send the request body.
            final Object body = request.getBody();

            if (body != null) {
                if (body instanceof byte[]) {
                    final byte[] bytes = (byte[]) body;
                    connection.setDoOutput(true);
                    try (OutputStream os = connection.getOutputStream()) {
                        os.write(bytes, 0, bytes.length);
                    }
                } else {
                    String str = null;
                    if (body instanceof String) {
                        str = (String) body;
                    } else {
                        str = JsonUtils.toString(body);
                    }

                    if (str != null && !str.isBlank()) {
                        connection.setDoOutput(true);
                        try (OutputStream os = connection.getOutputStream()) {
                            byte[] bytes = str.getBytes(request.getCharset());
                            os.write(bytes, 0, bytes.length);
                        }
                    }
                }
            }

            status = connection.getResponseCode();
            message = connection.getResponseMessage();
            headers = getResponseHeaders(connection);

            final HttpStatus resolved = HttpStatus.resolve(status);

            final InputStream is = resolved.isError() ? connection.getErrorStream() : connection.getInputStream();

            try {
                if (is != null) {
                    // TODO: improve and handle not text content
                    content = is.readAllBytes();
                }
            } finally {
                connection.disconnect();
            }

            final Response response = Response.of(status, headers, content, start);

            if (!resolved.isSuccessful()) {
                throw new HttpResponseException(message, request, response);
            }

            return response;
        } catch (IOException e) {
            final Response response = Response.of(status, headers, content, start);
            throw new HttpResponseException(e, request, response);
        }
    }

    @Override
    public CompletableFuture<Response> async(final Request<?> request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute(request);
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
