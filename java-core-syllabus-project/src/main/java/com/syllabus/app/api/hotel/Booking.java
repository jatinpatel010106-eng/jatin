package com.syllabus.app.api.hotel;

import java.time.LocalDate;

public record Booking(Integer id,
                      String guestName,
                      String guestEmail,
                      int roomId,
                      LocalDate checkInDate,
                      LocalDate checkOutDate,
                      String bookingStatus) {
}
