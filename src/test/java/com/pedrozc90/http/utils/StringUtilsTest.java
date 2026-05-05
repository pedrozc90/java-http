package com.pedrozc90.http.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {

    @Test
    void isBlank_null() {
        assertTrue(StringUtils.isBlank(null));
    }

    @Test
    void isBlank_empty() {
        assertTrue(StringUtils.isBlank(""));
    }

    @Test
    void isBlank_whitespace() {
        assertTrue(StringUtils.isBlank("   "));
        assertTrue(StringUtils.isBlank("\t\n"));
    }

    @Test
    void isBlank_nonEmpty() {
        assertFalse(StringUtils.isBlank("hello"));
        assertFalse(StringUtils.isBlank("  x  "));
    }

    @Test
    void isNotBlank_null() {
        assertFalse(StringUtils.isNotBlank(null));
    }

    @Test
    void isNotBlank_empty() {
        assertFalse(StringUtils.isNotBlank(""));
    }

    @Test
    void isNotBlank_nonEmpty() {
        assertTrue(StringUtils.isNotBlank("hello"));
        assertTrue(StringUtils.isNotBlank("  x  "));
    }

}
