package com.pedrozc90.http.enums;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@ToString
public enum ContentType {

    // text/*
    TEXT_PLAIN("text/plain"),
    TEXT_HTML("text/html"),
    TEXT_XML("text/xml"),
    TEXT_CSS("text/css"),
    TEXT_JAVASCRIPT("text/javascript"),
    TEXT_CSV("text/csv"),
    TEXT_MARKDOWN("text/markdown"),
    TEXT_EVENT_STREAM("text/event-stream"),

    // application/*
    APPLICATION_JSON("application/json"),
    APPLICATION_XML("application/xml"),
    APPLICATION_PDF("application/pdf"),
    APPLICATION_OCTET_STREAM("application/octet-stream"),
    APPLICATION_FORM_URLENCODED("application/x-www-form-urlencoded"),
    APPLICATION_GRAPHQL("application/graphql"),
    APPLICATION_YAML("application/x-yaml"),
    APPLICATION_ZIP("application/zip"),
    APPLICATION_GZIP("application/gzip"),

    // multipart/*
    MULTIPART_FORM_DATA("multipart/form-data"),

    // image/*
    IMAGE_PNG("image/png"),
    IMAGE_JPEG("image/jpeg"),
    IMAGE_GIF("image/gif"),
    IMAGE_WEBP("image/webp"),
    IMAGE_SVG_XML("image/svg+xml");

    private static final Map<String, ContentType> _map = new HashMap<>();

    static {
        for (final ContentType type : values()) {
            _map.put(type.value, type);
        }
    }

    private final String value;

    public String value() {
        return value;
    }

    public static ContentType resolve(final String value) {
        if (value == null) return null;
        return _map.getOrDefault(value, ContentType.APPLICATION_OCTET_STREAM);
    }

}
