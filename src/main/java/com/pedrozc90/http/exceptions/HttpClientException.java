package com.pedrozc90.http.exceptions;

/**
 * Exception thrown when an HTTP client operation fails.
 */
public class HttpClientException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public HttpClientException(final String message) {
        super(message);
    }

    public HttpClientException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public HttpClientException(final Throwable cause) {
        super(cause);
    }
}
