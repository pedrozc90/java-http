package com.pedrozc90.http.exceptions;

import com.pedrozc90.http.objects.Request;
import com.pedrozc90.http.objects.Response;
import lombok.Getter;

import java.util.concurrent.CompletionException;

/**
 * Exception thrown when an HTTP client operation fails.
 */
@Getter
public class HttpResponseException extends Exception {

    private final Request<?> request;
    private final Response response;

    public HttpResponseException(final Throwable cause, final String message, final Request<?> request, final Response response) {
        super(message, cause);
        this.request = request;
        this.response = response;
    }

    public HttpResponseException(final Throwable cause, final Request<?> request, final Response response) {
        this(cause, null, request, response);
    }

    public HttpResponseException(final String message, final Request<?> request, final Response response) {
        this(null, message, request, response);
    }

    public CompletionException toCompletionException() {
        return new CompletionException(this);
    }

}
