package com.pedrozc90.http.utils;

/**
 * Minimal string guard utilities.
 */
public class StringUtils {

    private StringUtils() {}

    /**
     * Returns {@code true} when the value is {@code null} or contains only whitespace.
     */
    public static boolean isBlank(final String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Returns {@code true} when the value is neither {@code null} nor blank.
     */
    public static boolean isNotBlank(final String value) {
        return !isBlank(value);
    }

}
