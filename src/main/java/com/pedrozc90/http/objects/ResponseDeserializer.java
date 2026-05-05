package com.pedrozc90.http.objects;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.pedrozc90.http.enums.HttpStatus;
import com.pedrozc90.http.utils.JsonUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Jackson deserializer for {@link Response}.
 *
 * <p>Reverses the smart payload serialization performed by {@link ResponseSerializer}:
 * <ul>
 *   <li>JSON object/array — serialized back to compact UTF-8 bytes</li>
 *   <li>text string — converted to UTF-8 bytes</li>
 *   <li>null or absent — {@code null}</li>
 * </ul>
 */
public class ResponseDeserializer extends StdDeserializer<Response> {

    public ResponseDeserializer() {
        super(Response.class);
    }

    @Override
    public Response deserialize(final JsonParser p, final DeserializationContext ctx) throws IOException {
        final JsonNode node = p.getCodec().readTree(p);
        if (node == null || node.isNull()) return null;

        final HttpStatus status = node.has("status")
            ? HttpStatus.resolve(node.get("status").asInt(-1))
            : HttpStatus.NONE;

        final Map<String, String> headers = (node.has("headers"))
            ? JsonUtils.map(node.get("headers"))
            : null;

        final long elapsed = node.has("elapsed")
            ? node.get("elapsed").asLong(0L)
            : 0L;

        final byte[] payload = deserializePayload(node.get("payload"));

        return new Response(status, headers, payload, elapsed);
    }

    private static byte[] deserializePayload(final JsonNode payloadNode) throws IOException {
        if (payloadNode == null || payloadNode.isNull()) return null;

        if (payloadNode.isTextual()) {
            return payloadNode.asText().getBytes(StandardCharsets.UTF_8);
        }

        // JSON object or array — write back to compact JSON bytes
        return JsonUtils.toBytes(payloadNode);
    }

}
