package com.pedrozc90.http.exceptions;

import java.io.IOException;

/**
 * Thrown when a JSON serialization or deserialization operation fails.
 *
 * <p>Wraps underlying JSON-library exceptions (e.g. Jackson's
 * {@link com.fasterxml.jackson.core.JsonProcessingException}) so callers
 * can catch a single, library-agnostic type for all JSON failures.
 */
public class JsonException extends IOException {

    public JsonException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public JsonException(final Throwable cause) {
        super(cause);
    }

    public JsonException(final String message) {
        super(message);
    }

}
