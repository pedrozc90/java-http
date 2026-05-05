package com.pedrozc90.http.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HeaderUtils {

    private HeaderUtils() {
        // you are not allowed to instantiate this class
    }

    /**
     * Case-insensitive header lookup. Returns the first matching value, or {@code null}.
     */
    public static String findHeader(final Map<String, String> headers, final String name) {
        if (headers == null || name == null) return null;
        for (final Map.Entry<String, String> entry : headers.entrySet()) {
            if (name.equalsIgnoreCase(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Parses the {@code charset} parameter from a {@code Content-Type} header value
     * (e.g. {@code text/html; charset=UTF-8}).  Returns {@link StandardCharsets#UTF_8}
     * when no charset is present or the name is unrecognised.
     */
    public static Charset parseCharset(final String contentType) {
        if (StringUtils.isBlank(contentType)) return StandardCharsets.UTF_8;
        for (final String part : contentType.split(";")) {
            final String trimmed = part.trim();
            if (trimmed.toLowerCase().startsWith("charset=")) {
                String name = trimmed.substring("charset=".length()).trim();
                if (name.startsWith("\"") && name.endsWith("\"")) {
                    name = name.substring(1, name.length() - 1);
                }
                try {
                    return Charset.forName(name);
                } catch (Exception ignored) {
                    return StandardCharsets.UTF_8;
                }
            }
        }
        return StandardCharsets.UTF_8;
    }

    public static String getFilenameFromContentDisposition(final String value) {
        if (StringUtils.isBlank(value)) return null;

        for (final String part : value.split(";")) {
            final String trimmed = part.trim();
            if (trimmed.startsWith("filename=")) {
                String name = trimmed.substring("filename=".length()).trim();
                if (name.startsWith("\"") && name.endsWith("\"")) {
                    name = name.substring(1, name.length() - 1);
                }
                return name;
            }
        }
        return null;
    }

}
