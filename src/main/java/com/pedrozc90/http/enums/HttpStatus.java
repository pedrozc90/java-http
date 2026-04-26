package com.pedrozc90.http.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration of standard HTTP status codes with their reason phrases.
 */
public enum HttpStatus {

    // 1xx Informational
    CONTINUE(100, "Continue"),
    SWITCHING_PROTOCOLS(101, "Switching Protocols"),
    PROCESSING(102, "Processing"),

    // 2xx Success
    OK(200, "OK"),
    CREATED(201, "Created"),
    ACCEPTED(202, "Accepted"),
    NON_AUTHORITATIVE_INFORMATION(203, "Non-Authoritative Information"),
    NO_CONTENT(204, "No Content"),
    RESET_CONTENT(205, "Reset Content"),
    PARTIAL_CONTENT(206, "Partial Content"),

    // 3xx Redirection
    MULTIPLE_CHOICES(300, "Multiple Choices"),
    MOVED_PERMANENTLY(301, "Moved Permanently"),
    FOUND(302, "Found"),
    SEE_OTHER(303, "See Other"),
    NOT_MODIFIED(304, "Not Modified"),
    TEMPORARY_REDIRECT(307, "Temporary Redirect"),
    PERMANENT_REDIRECT(308, "Permanent Redirect"),

    // 4xx Client Errors
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    PAYMENT_REQUIRED(402, "Payment Required"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    NOT_ACCEPTABLE(406, "Not Acceptable"),
    PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
    REQUEST_TIMEOUT(408, "Request Timeout"),
    CONFLICT(409, "Conflict"),
    GONE(410, "Gone"),
    LENGTH_REQUIRED(411, "Length Required"),
    PRECONDITION_FAILED(412, "Precondition Failed"),
    PAYLOAD_TOO_LARGE(413, "Payload Too Large"),
    URI_TOO_LONG(414, "URI Too Long"),
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
    RANGE_NOT_SATISFIABLE(416, "Range Not Satisfiable"),
    EXPECTATION_FAILED(417, "Expectation Failed"),
    I_AM_A_TEAPOT(418, "I'm a Teapot"),
    UNPROCESSABLE_ENTITY(422, "Unprocessable Entity"),
    LOCKED(423, "Locked"),
    FAILED_DEPENDENCY(424, "Failed Dependency"),
    TOO_EARLY(425, "Too Early"),
    UPGRADE_REQUIRED(426, "Upgrade Required"),
    PRECONDITION_REQUIRED(428, "Precondition Required"),
    TOO_MANY_REQUESTS(429, "Too Many Requests"),
    REQUEST_HEADER_FIELDS_TOO_LARGE(431, "Request Header Fields Too Large"),
    UNAVAILABLE_FOR_LEGAL_REASONS(451, "Unavailable For Legal Reasons"),

    // 5xx Server Errors
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    NOT_IMPLEMENTED(501, "Not Implemented"),
    BAD_GATEWAY(502, "Bad Gateway"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    GATEWAY_TIMEOUT(504, "Gateway Timeout"),
    HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported"),
    VARIANT_ALSO_NEGOTIATES(506, "Variant Also Negotiates"),
    INSUFFICIENT_STORAGE(507, "Insufficient Storage"),
    LOOP_DETECTED(508, "Loop Detected"),
    NOT_EXTENDED(510, "Not Extended"),
    NETWORK_AUTHENTICATION_REQUIRED(511, "Network Authentication Required");

    private final int value;
    private final String reasonPhrase;

    HttpStatus(final int value, final String reasonPhrase) {
        this.value = value;
        this.reasonPhrase = reasonPhrase;
    }

    /**
     * Returns the numeric status code.
     */
    @JsonValue
    public int value() {
        return value;
    }

    /**
     * Returns the human-readable reason phrase.
     */
    public String getReasonPhrase() {
        return reasonPhrase;
    }

    /**
     * Returns {@code true} if this status is in the 1xx Informational range.
     */
    public boolean is1xxInformational() {
        return value >= 100 && value < 200;
    }

    /**
     * Returns {@code true} if this status is in the 2xx Successful range.
     */
    public boolean is2xxSuccessful() {
        return value >= 200 && value < 300;
    }

    /**
     * Returns {@code true} if this status is in the 3xx Redirection range.
     */
    public boolean is3xxRedirection() {
        return value >= 300 && value < 400;
    }

    /**
     * Returns {@code true} if this status is in the 4xx Client Error range.
     */
    public boolean is4xxClientError() {
        return value >= 400 && value < 500;
    }

    /**
     * Returns {@code true} if this status is in the 5xx Server Error range.
     */
    public boolean is5xxServerError() {
        return value >= 500 && value < 600;
    }

    /**
     * Returns {@code true} if this status is either a client or server error.
     */
    public boolean isError() {
        return is4xxClientError() || is5xxServerError();
    }

    /**
     * Resolves a {@link HttpStatus} for the given numeric status code.
     *
     * @param statusCode the HTTP status code
     * @return the matching {@link HttpStatus}
     * @throws IllegalArgumentException if no matching constant is found
     */
    @JsonCreator
    public static HttpStatus resolve(final int statusCode) {
        for (final HttpStatus status : values()) {
            if (status.value == statusCode) {
                return status;
            }
        }
        throw new IllegalArgumentException("No matching HttpStatus for status code: " + statusCode);
    }

    @Override
    public String toString() {
        return value + " " + reasonPhrase;
    }
}
