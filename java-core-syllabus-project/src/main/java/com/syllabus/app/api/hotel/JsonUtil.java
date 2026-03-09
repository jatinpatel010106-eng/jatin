package com.syllabus.app.api.hotel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class JsonUtil {
    private JsonUtil() {
    }

    public static String readBody(InputStream inputStream) throws IOException {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).trim();
    }

    // Minimal parser for flat JSON objects used in this project.
    public static Map<String, String> parseFlatJson(String body) {
        Map<String, String> data = new HashMap<>();
        if (body == null || body.isBlank()) {
            return data;
        }

        String json = body.trim();
        if (json.startsWith("{")) {
            json = json.substring(1);
        }
        if (json.endsWith("}")) {
            json = json.substring(0, json.length() - 1);
        }

        if (json.isBlank()) {
            return data;
        }

        String[] pairs = json.split(",");
        for (String pair : pairs) {
            String[] parts = pair.split(":", 2);
            if (parts.length != 2) {
                continue;
            }
            String key = unquote(parts[0].trim());
            String value = unquote(parts[1].trim());
            data.put(key, value);
        }
        return data;
    }

    private static String unquote(String value) {
        String clean = value.trim();
        if (clean.startsWith("\"") && clean.endsWith("\"")) {
            clean = clean.substring(1, clean.length() - 1);
        }
        return clean;
    }

    public static String roomToJson(Room room) {
        return String.format(
                "{\"id\":%d,\"roomNumber\":\"%s\",\"type\":\"%s\",\"pricePerNight\":%.2f,\"available\":%s}",
                room.id(), escape(room.roomNumber()), escape(room.type()), room.pricePerNight(), room.available());
    }

    public static String bookingToJson(Booking booking) {
        return String.format(
                "{\"id\":%d,\"guestName\":\"%s\",\"guestEmail\":\"%s\",\"roomId\":%d,\"checkInDate\":\"%s\",\"checkOutDate\":\"%s\",\"bookingStatus\":\"%s\"}",
                booking.id(), escape(booking.guestName()), escape(booking.guestEmail()), booking.roomId(),
                booking.checkInDate(), booking.checkOutDate(), escape(booking.bookingStatus()));
    }

    public static String listToJson(List<String> items) {
        return items.stream().collect(Collectors.joining(",", "[", "]"));
    }

    private static String escape(String text) {
        return text.replace("\"", "\\\"");
    }
}
