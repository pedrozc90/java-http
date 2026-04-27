package com.pedrozc90.http.utils;

import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HeaderUtilsTest {

    // -------------------------------------------------------------------------
    // findHeader
    // -------------------------------------------------------------------------

    @Test
    void findHeader_exactMatch() {
        final Map<String, String> headers = Collections.singletonMap("Content-Type", "application/json");
        assertEquals("application/json", HeaderUtils.findHeader(headers, "Content-Type"));
    }

    @Test
    void findHeader_caseInsensitive() {
        final Map<String, String> headers = Collections.singletonMap("content-type", "text/plain");
        assertEquals("text/plain", HeaderUtils.findHeader(headers, "Content-Type"));
    }

    @Test
    void findHeader_missing() {
        final Map<String, String> headers = Collections.singletonMap("Accept", "application/json");
        assertNull(HeaderUtils.findHeader(headers, "Content-Type"));
    }

    @Test
    void findHeader_nullHeaders() {
        assertNull(HeaderUtils.findHeader(null, "Content-Type"));
    }

    @Test
    void findHeader_nullName() {
        assertNull(HeaderUtils.findHeader(Collections.singletonMap("k", "v"), null));
    }

    // -------------------------------------------------------------------------
    // parseCharset
    // -------------------------------------------------------------------------

    @Test
    void parseCharset_utf8Explicit() {
        assertEquals(StandardCharsets.UTF_8, HeaderUtils.parseCharset("text/html; charset=UTF-8"));
    }

    @Test
    void parseCharset_iso88591() {
        final Charset result = HeaderUtils.parseCharset("text/html; charset=ISO-8859-1");
        assertEquals(Charset.forName("ISO-8859-1"), result);
    }

    @Test
    void parseCharset_noCharset_defaultsToUtf8() {
        assertEquals(StandardCharsets.UTF_8, HeaderUtils.parseCharset("application/json"));
    }

    @Test
    void parseCharset_null_defaultsToUtf8() {
        assertEquals(StandardCharsets.UTF_8, HeaderUtils.parseCharset(null));
    }

    @Test
    void parseCharset_blank_defaultsToUtf8() {
        assertEquals(StandardCharsets.UTF_8, HeaderUtils.parseCharset("   "));
    }

    @Test
    void parseCharset_unknown_defaultsToUtf8() {
        assertEquals(StandardCharsets.UTF_8, HeaderUtils.parseCharset("text/plain; charset=INVALID-9999"));
    }

    @Test
    void parseCharset_quotedCharset() {
        assertEquals(StandardCharsets.UTF_8, HeaderUtils.parseCharset("text/html; charset=\"UTF-8\""));
    }

    // -------------------------------------------------------------------------
    // getFilenameFromContentDisposition
    // -------------------------------------------------------------------------

    @Test
    void getFilenameFromContentDisposition_quoted() {
        assertEquals("report.pdf", HeaderUtils.getFilenameFromContentDisposition("attachment; filename=\"report.pdf\""));
    }

    @Test
    void getFilenameFromContentDisposition_unquoted() {
        assertEquals("report.pdf", HeaderUtils.getFilenameFromContentDisposition("attachment; filename=report.pdf"));
    }

    @Test
    void getFilenameFromContentDisposition_null() {
        assertNull(HeaderUtils.getFilenameFromContentDisposition(null));
    }

    @Test
    void getFilenameFromContentDisposition_blank() {
        assertNull(HeaderUtils.getFilenameFromContentDisposition("  "));
    }

}
