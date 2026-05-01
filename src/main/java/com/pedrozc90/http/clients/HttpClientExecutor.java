package com.pedrozc90.http.clients;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Provides a shared default {@link Executor} for async HTTP operations.
 *
 * <p>The pool size defaults to {@code max(4, availableProcessors * 2)} so it scales
 * automatically on larger machines while still being bounded on small ones.
 * A custom executor can be supplied to {@link NativeHttpClient#NativeHttpClient(Executor)}
 * when the default pool does not meet application requirements.
 */
public final class HttpClientExecutor {

    private static final int DEFAULT_POOL_SIZE =
        Math.max(4, Runtime.getRuntime().availableProcessors() * 2);

    private static final Executor DEFAULT_EXECUTOR =
        Executors.newFixedThreadPool(DEFAULT_POOL_SIZE);

    private HttpClientExecutor() {
    }

    /**
     * Returns the shared default executor backed by a fixed thread pool.
     *
     * @return the default {@link Executor}
     */
    public static Executor defaultExecutor() {
        return DEFAULT_EXECUTOR;
    }

}
