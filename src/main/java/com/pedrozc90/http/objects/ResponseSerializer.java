package com.pedrozc90.http.objects;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.pedrozc90.http.enums.HttpHeader;
import com.pedrozc90.http.utils.HeaderUtils;
import com.pedrozc90.http.utils.JsonUtils;

import java.io.IOException;

/**
 * Jackson serializer for {@link Response}.
 *
 * <p>The {@code payload} field is rendered based on the response {@code Content-Type}:
 * <ul>
 *   <li>{@code application/json} — payload is pretty-printed as a JSON tree</li>
 *   <li>{@code text/*} — payload is rendered as a string using the response charset</li>
 *   <li>anything else — a placeholder {@code <binary data, N bytes>} is emitted</li>
 * </ul>
 */
public class ResponseSerializer extends StdSerializer<Response> {

    public ResponseSerializer() {
        super(Response.class);
    }

    @Override
    public void serialize(final Response response, final JsonGenerator gen, final SerializerProvider provider) throws IOException {
        if (response == null) {
            gen.writeNull();
        } else {
            gen.writeStartObject();
            provider.defaultSerializeField("status", response.getStatus(), gen);
            provider.defaultSerializeField("headers", response.getHeaders(), gen);
            gen.writeNumberField("elapsed", response.getElapsed());
            serializePayload(response, gen);
            gen.writeEndObject();
        }
    }

    private static void serializePayload(final Response response, final JsonGenerator gen) throws IOException {
        gen.writeFieldName("payload");

        final byte[] payload = response.getPayload();
        if (payload == null || payload.length == 0) {
            gen.writeNull();
            return;
        }

        final String contentType = HeaderUtils.findHeader(response.getHeaders(), HttpHeader.CONTENT_TYPE);

        if (contentType != null && contentType.contains("application/json")) {
            try {
                final JsonNode json = JsonUtils.toJson(payload);
                gen.writeTree(json);
            } catch (JsonProcessingException e) {
                gen.writeString(new String(payload, response.getCharset()));
            }
        } else if (contentType != null && contentType.startsWith("text/")) {
            gen.writeString(new String(payload, response.getCharset()));
        } else {
            gen.writeString("<binary data, " + payload.length + " bytes>");
        }
    }

}
