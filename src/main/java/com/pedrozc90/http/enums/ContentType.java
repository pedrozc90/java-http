package com.pedrozc90.http.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public enum ContentType {

    TEXT_PLAIN("text/plain"),
    TEXT_HTML("text/html"),
    TEXT_XML("text/xml"),
    TEXT_CSS("text/css"),
    TEXT_JAVASCRIPT("text/javascript"),
    TEXT_CSV("text/csv"),

    APPLICATION_OCTET_STREAM("application/octet-stream"),
    APPLICATION_JSON("application/json"),
    APPLICATION_FORM_URLENCODED("application/x-www-form-urlencoded");

    private final String value;

}
