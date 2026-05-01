package com.pedrozc90.http.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.pedrozc90.http.exceptions.JsonException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Map;

public class JsonUtils {

    private JsonUtils() {
    }

    private static ObjectMapper _mapper = createMapper();

    private static ObjectMapper createMapper() {
        return new ObjectMapper()
            .configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
            .disable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS"));
    }

    public static ObjectMapper getMapper() {
        return _mapper;
    }

    public static void setMapper(final ObjectMapper mapper) {
        _mapper = mapper;
    }

    public static <T> T toObject(final JsonNode value, final Class<T> type) throws JsonException {
        if (value == null) return null;
        try {
            return _mapper.treeToValue(value, type);
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }

    public static <T> T toObject(final String value, final Class<T> type) throws JsonException {
        if (value == null) return null;
        try {
            return _mapper.readValue(value, type);
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }

    public static <T> T toObject(final byte[] value, final Class<T> type) throws JsonException {
        if (value == null) return null;
        try {
            return _mapper.readValue(value, type);
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }

    public static JsonNode toJson(final Object value) {
        if (value == null) return null;
        return _mapper.valueToTree(value);
    }

    public static JsonNode toJson(final String value) throws JsonException {
        try {
            return _mapper.readTree(value);
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }

    public static String toString(final Object value) throws JsonException {
        if (value == null) return null;
        try {
            return _mapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }

    public static String toString(final JsonNode value) {
        if (value == null) return null;
        return value.toPrettyString();
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> map(final JsonNode node) {
        if (node == null) return null;
        return _mapper.convertValue(node, Map.class);
    }

    public static byte[] toBytes(final JsonNode node) throws JsonException {
        if (node == null) return null;
        try {
            return _mapper.writeValueAsBytes(node);
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }

}
