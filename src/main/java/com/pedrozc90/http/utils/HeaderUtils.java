package com.pedrozc90.http.utils;

public class HeaderUtils {

    public static String getFilenameFromContentDisposition(final String value) {
        if (value == null || value.isBlank()) return null;

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
