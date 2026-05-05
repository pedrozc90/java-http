package com.pedrozc90.http.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pedrozc90.http.exceptions.JsonException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JsonUtilsTest {

    private final ObjectMapper mapper = JsonUtils.getMapper();

    @Test
    public void convertStringToJson() throws JsonException {
        final String value = "{\"name\":\"Pedro\"}";
        final JsonNode result = JsonUtils.toJson(value);
        assertNotNull(result);
        assertTrue(result.has("name"));
        assertEquals("Pedro", result.get("name").asText());
    }

    @Test
    public void convertStringToObject() throws JsonException {
        final String value = "{\"name\":\"Pedro\"}";
        final Dto result = JsonUtils.toObject(value, Dto.class);
        assertNotNull(result);
        assertEquals("Pedro", result.getName());
    }

    @Test
    public void convertJsonToString() throws JsonException {
        final ObjectNode obj = mapper.createObjectNode();
        obj.put("name", "Pedro");

        final JsonNode value = (JsonNode) obj;
        final String result = JsonUtils.toString(value);
        assertNotNull(result);
        assertTrue(result.contains("\"name\" : \"Pedro\""));
    }

    @Test
    public void convertObjectToString() throws JsonException {
        final Dto value = new Dto("Pedro");
        final String result = JsonUtils.toString(value);
        assertNotNull(result);
        assertTrue(result.contains("\"name\" : \"Pedro\""));
    }

    @Test
    public void invalidJsonStringThrowsJsonException() {
        assertThrows(JsonException.class, () -> JsonUtils.toJson("{invalid json}"));
    }

    @Test
    public void invalidJsonBytesThrowsJsonException() {
        assertThrows(JsonException.class, () -> JsonUtils.toObject("{bad}".getBytes(), Dto.class));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Dto {

        @JsonProperty(value = "name")
        private String name;

    }

}
