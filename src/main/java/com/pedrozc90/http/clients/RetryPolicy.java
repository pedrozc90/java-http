package com.pedrozc90.http.clients;

import com.pedrozc90.http.enums.HttpStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Configuration for the retry behaviour of an HTTP request.
 *
 * <p>Set a policy on any {@link com.pedrozc90.http.objects.Request} via the builder's
 * {@code retryPolicy(RetryPolicy)} step; {@link NativeHttpClient} will then automatically
 * retry that request according to this configuration:
 *
 * <pre>{@code
 * RetryPolicy policy = RetryPolicy.builder()
 *     .maxAttempts(3)
 *     .delayMs(500)
 *     .retryOn(429, 503)
 *     .build();
 *
 * Request<?> request = Request.builder()
 *     .url("https://api.example.com/data")
 *     .retryPolicy(policy)
 *     .get()
 *     .build();
 * }</pre>
 */
public final class RetryPolicy {

    /** Default status codes that warrant an automatic retry. */
    private static final Set<Integer> DEFAULT_RETRYABLE_STATUS_CODES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(429, 502, 503, 504)));

    private final int maxAttempts;
    private final long delayMs;
    private final Set<Integer> retryableStatusCodes;
    private final boolean retryOnException;

    private RetryPolicy(final Builder builder) {
        this.maxAttempts = builder.maxAttempts;
        this.delayMs = builder.delayMs;
        this.retryableStatusCodes = Collections.unmodifiableSet(new HashSet<>(builder.retryableStatusCodes));
        this.retryOnException = builder.retryOnException;
    }

    /**
     * Maximum number of total attempts (first attempt + retries).
     * Must be at least {@code 1}.
     */
    public int getMaxAttempts() {
        return maxAttempts;
    }

    /**
     * Milliseconds to wait between consecutive attempts.
     */
    public long getDelayMs() {
        return delayMs;
    }

    /**
     * HTTP status codes that should trigger an automatic retry.
     */
    public Set<Integer> getRetryableStatusCodes() {
        return retryableStatusCodes;
    }

    /**
     * Whether a network-level exception (e.g. connection timeout) should also
     * trigger a retry in addition to retryable status codes.
     */
    public boolean isRetryOnException() {
        return retryOnException;
    }

    /**
     * Returns {@code true} if the given HTTP status code is configured as retryable.
     *
     * @param status the HTTP status code to check
     * @return {@code true} if the code should trigger a retry
     */
    public boolean isRetryable(final int status) {
        return retryableStatusCodes.contains(status);
    }

    public boolean isRetryable(final HttpStatus status) {
        if (status == null) return false;
        return retryableStatusCodes.contains(status.code());
    }

    /**
     * Sleeps for the given number of milliseconds, swallowing {@link InterruptedException}
     * and restoring the thread interrupt flag.
     *
     * @param millis milliseconds to sleep; values &le; 0 are ignored
     */
    static void sleep(final long millis) {
        if (millis <= 0) return;
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Returns a new builder pre-configured with sensible defaults:
     * 3 attempts, 1 s delay, retry on 429 / 502 / 503 / 504, retry on exception.
     *
     * @return a new {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private int maxAttempts = 3;
        private long delayMs = 1_000;
        private Set<Integer> retryableStatusCodes = new HashSet<>(DEFAULT_RETRYABLE_STATUS_CODES);
        private boolean retryOnException = true;

        private Builder() {
        }

        /**
         * Sets the maximum number of total attempts (must be &ge; 1).
         *
         * @param maxAttempts total attempts including the first one
         * @return this builder
         */
        public Builder maxAttempts(final int maxAttempts) {
            if (maxAttempts < 1) throw new IllegalArgumentException("maxAttempts must be >= 1");
            this.maxAttempts = maxAttempts;
            return this;
        }

        /**
         * Sets the delay in milliseconds between consecutive attempts.
         *
         * @param delayMs delay in milliseconds; negative values are treated as zero
         * @return this builder
         */
        public Builder delayMs(final long delayMs) {
            this.delayMs = Math.max(0, delayMs);
            return this;
        }

        /**
         * Replaces the set of retryable HTTP status codes.
         *
         * @param statusCodes the HTTP status codes that trigger a retry
         * @return this builder
         */
        public Builder retryOn(final Integer... statusCodes) {
            this.retryableStatusCodes = new HashSet<>(Arrays.asList(statusCodes));
            return this;
        }

        /**
         * Sets whether network-level exceptions should also trigger a retry.
         *
         * @param retryOnException {@code true} to retry on exception
         * @return this builder
         */
        public Builder retryOnException(final boolean retryOnException) {
            this.retryOnException = retryOnException;
            return this;
        }

        /**
         * Builds a new {@link RetryPolicy} from the current builder state.
         *
         * @return a new {@link RetryPolicy}
         */
        public RetryPolicy build() {
            return new RetryPolicy(this);
        }
    }
}
