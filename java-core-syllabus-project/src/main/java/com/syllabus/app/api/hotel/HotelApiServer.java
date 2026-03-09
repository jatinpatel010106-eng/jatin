package com.syllabus.app.api.hotel;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.syllabus.app.db.DatabaseManager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class HotelApiServer {
    private final HttpServer server;

    public HotelApiServer(int port) throws IOException {
        HotelDao hotelDao = new HotelDao(new DatabaseManager());

        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/health", new HealthHandler());
        server.createContext("/api/rooms", new RoomHandler(hotelDao));
        server.createContext("/api/bookings", new BookingHandler(hotelDao));
        server.setExecutor(Executors.newFixedThreadPool(10));
    }

    public void start() {
        server.start();
        System.out.println("Hotel API running on http://localhost:" + server.getAddress().getPort());
    }

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("HOTEL_API_PORT", "8081"));
        new HotelApiServer(port).start();
    }

    private static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            writeJson(exchange, 200, "{\"status\":\"ok\"}");
        }
    }

    private static class RoomHandler implements HttpHandler {
        private final HotelDao hotelDao;

        private RoomHandler(HotelDao hotelDao) {
            this.hotelDao = hotelDao;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    List<String> rooms = hotelDao.getRooms().stream()
                            .map(JsonUtil::roomToJson)
                            .toList();
                    writeJson(exchange, 200, JsonUtil.listToJson(rooms));
                    return;
                }
                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    Map<String, String> payload = JsonUtil.parseFlatJson(JsonUtil.readBody(exchange.getRequestBody()));
                    Room room = new Room(
                            null,
                            payload.get("roomNumber"),
                            payload.get("type"),
                            Double.parseDouble(payload.get("pricePerNight")),
                            Boolean.parseBoolean(payload.getOrDefault("available", "true")));
                    Room created = hotelDao.createRoom(room);
                    writeJson(exchange, 201, JsonUtil.roomToJson(created));
                    return;
                }
                writeJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
            } catch (SQLException | RuntimeException e) {
                writeJson(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    private static class BookingHandler implements HttpHandler {
        private final HotelDao hotelDao;

        private BookingHandler(HotelDao hotelDao) {
            this.hotelDao = hotelDao;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    List<String> bookings = hotelDao.getBookings().stream()
                            .map(JsonUtil::bookingToJson)
                            .toList();
                    writeJson(exchange, 200, JsonUtil.listToJson(bookings));
                    return;
                }
                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    Map<String, String> payload = JsonUtil.parseFlatJson(JsonUtil.readBody(exchange.getRequestBody()));
                    Booking booking = HotelDao.parseBookingPayload(
                            payload.get("guestName"),
                            payload.get("guestEmail"),
                            Integer.parseInt(payload.get("roomId")),
                            payload.get("checkInDate"),
                            payload.get("checkOutDate"),
                            payload.getOrDefault("bookingStatus", "CONFIRMED"));
                    Booking created = hotelDao.createBooking(booking);
                    writeJson(exchange, 201, JsonUtil.bookingToJson(created));
                    return;
                }
                writeJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
            } catch (SQLException | RuntimeException e) {
                writeJson(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    private static void writeJson(HttpExchange exchange, int statusCode, String payload) throws IOException {
        byte[] response = payload.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(response);
        }
    }
}
