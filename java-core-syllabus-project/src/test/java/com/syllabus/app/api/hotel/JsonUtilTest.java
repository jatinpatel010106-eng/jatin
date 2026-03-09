package com.syllabus.app.api.hotel;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonUtilTest {

    @Test
    void parseFlatJson_shouldParseRoomPayload() {
        String payload = "{\"roomNumber\":\"A-101\",\"type\":\"DELUXE\",\"pricePerNight\":\"3500\"}";

        Map<String, String> parsed = JsonUtil.parseFlatJson(payload);

        assertEquals("A-101", parsed.get("roomNumber"));
        assertEquals("DELUXE", parsed.get("type"));
        assertEquals("3500", parsed.get("pricePerNight"));
    }

    @Test
    void bookingToJson_shouldContainKeyFields() {
        Booking booking = new Booking(1, "John", "john@example.com", 2,
                LocalDate.parse("2026-01-10"), LocalDate.parse("2026-01-12"), "CONFIRMED");

        String json = JsonUtil.bookingToJson(booking);

        assertTrue(json.contains("\"guestName\":\"John\""));
        assertTrue(json.contains("\"roomId\":2"));
    }
}
