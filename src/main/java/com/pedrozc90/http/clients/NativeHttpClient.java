package com.pedrozc90.http.clients;

import com.pedrozc90.http.enums.HttpStatus;
import com.pedrozc90.http.exceptions.HttpResponseException;
import com.pedrozc90.http.exceptions.JsonException;
import com.pedrozc90.http.objects.Request;
import com.pedrozc90.http.objects.Response;
import com.pedrozc90.http.utils.JsonUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class NativeHttpClient implements HttpClient {

    @Override
    public Response execute(final Request<?> request) throws HttpResponseException {
        final long start = System.currentTimeMillis();

        int status = -1;
        String message = null;
        byte[] content = null;
        Map<String, String> headers = null;

        try {
            // create a url object from the url.
            final URL url = new URL(request.getUrl());

            final int timeout = request.getTimeout();

            // open a connection to the URL.
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(request.getMethod().name());
            connection.setRequestProperty("Connection", "keep-alive");
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);

            // set the request headers.
            request.getHeaders().forEach((key, value) -> connection.setRequestProperty(key, value));

            // send the request body.
            final byte[] bytes = getBodyBytes(request);
            if (bytes != null && bytes.length > 0) {
                connection.setDoOutput(true);
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(bytes, 0, bytes.length);
                }
            }

            status = connection.getResponseCode();
            message = connection.getResponseMessage();
            headers = getResponseHeaders(connection);

            final HttpStatus resolved = HttpStatus.resolve(status);

            // read response body
            try (final InputStream is = resolved.isError() ? connection.getErrorStream() : connection.getInputStream()) {
                if (is != null) {
                    content = readBytes(is);
                }

                final Response response = Response.of(status, headers, content, start);
                if (!response.isSuccessful()) {
                    throw new HttpResponseException(message, request, response);
                }
                return response;
            } finally {
                connection.disconnect();
            }
        } catch (IOException e) {
            final Response response = Response.of(status, headers, content, start);
            throw new HttpResponseException(e, request, response);
        }
    }

    public CompletableFuture<Response> async(final Request<?> request, final Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute(request);
            } catch (HttpResponseException e) {
                // TODO: how do we properly handle this?
                throw e.toCompletionException();
            }
        }, executor);
    }

    /* --- Helpers --- */
    private <T> byte[] getBodyBytes(final Request<T> request) throws IOException {
        final T body = request.getBody();

        if (body == null) return null;

        if (body instanceof byte[]) {
            return (byte[]) body;
        } else if (body instanceof String) {
            final String str = (String) body;
            return str.getBytes(request.getCharset());
        } else {
            try {
                final String json = JsonUtils.toString(body);
                return json.getBytes(request.getCharset());
            } catch (JsonException e) {
                throw new IOException("Failed to serialize body", e);
            }
        }
    }

    private Map<String, String> getResponseHeaders(final HttpURLConnection connection) {
        final Map<String, String> out = new HashMap<>();

        final Map<String, List<String>> headers = connection.getHeaderFields();
        if (headers == null || headers.isEmpty()) return out;

        headers.forEach((key, value) -> {
            if (key == null) return; // skip the status-line pseudo-header
            out.put(key, String.join("; ", value));
        });

        return out;
    }

    private static byte[] readBytes(final InputStream is) throws IOException {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final byte[] chunk = new byte[8192];
        int nRead;
        while ((nRead = is.read(chunk)) != -1) {
            buffer.write(chunk, 0, nRead);
        }
        return buffer.toByteArray();
    }

}
