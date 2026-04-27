package com.pedrozc90.http.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
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

    private final String value;

}
