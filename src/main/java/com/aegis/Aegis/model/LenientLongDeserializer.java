 package com.aegis.Aegis.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeParseException;

/**
 * Deserializes a JSON value into a long epoch-milliseconds. Accepts either a numeric value
 * (milliseconds since epoch) or an ISO-8601 timestamp string (e.g. "2026-03-02T13:30:00Z").
 */
public class
LenientLongDeserializer extends JsonDeserializer<Long> {
    @Override
    public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getText();
        if (text == null) {
            return 0L;
        }
        text = text.trim();
        if (text.isEmpty()) {
            return 0L;
        }

        // Try parse as long
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            // Try parse as ISO-8601
            try {
                Instant inst = Instant.parse(text);
                return inst.toEpochMilli();
            } catch (DateTimeParseException ex) {
                throw new IOException("Cannot deserialize value to epoch millis: " + text);
            }
        }
    }
}
