package com.syllabus.app.api.hotel;

public record Room(Integer id, String roomNumber, String type, double pricePerNight, boolean available) {
}
