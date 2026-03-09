package com.syllabus.app.api.hotel;

import com.syllabus.app.db.DatabaseManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HotelDao {
    private final DatabaseManager databaseManager;

    public HotelDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public List<Room> getRooms() throws SQLException {
        String sql = "SELECT id, room_number, room_type, price_per_night, is_available FROM hotel_rooms ORDER BY id";
        List<Room> rooms = new ArrayList<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                rooms.add(new Room(
                        resultSet.getInt("id"),
                        resultSet.getString("room_number"),
                        resultSet.getString("room_type"),
                        resultSet.getDouble("price_per_night"),
                        resultSet.getBoolean("is_available")
                ));
            }
        }
        return rooms;
    }

    public Room createRoom(Room room) throws SQLException {
        String sql = "INSERT INTO hotel_rooms(room_number, room_type, price_per_night, is_available) VALUES(?,?,?,?)";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, room.roomNumber());
            statement.setString(2, room.type());
            statement.setDouble(3, room.pricePerNight());
            statement.setBoolean(4, room.available());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return new Room(keys.getInt(1), room.roomNumber(), room.type(), room.pricePerNight(), room.available());
                }
            }
        }
        throw new SQLException("Unable to create room");
    }

    public List<Booking> getBookings() throws SQLException {
        String sql = "SELECT id, guest_name, guest_email, room_id, check_in_date, check_out_date, booking_status FROM hotel_bookings ORDER BY id";
        List<Booking> bookings = new ArrayList<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                bookings.add(new Booking(
                        resultSet.getInt("id"),
                        resultSet.getString("guest_name"),
                        resultSet.getString("guest_email"),
                        resultSet.getInt("room_id"),
                        resultSet.getDate("check_in_date").toLocalDate(),
                        resultSet.getDate("check_out_date").toLocalDate(),
                        resultSet.getString("booking_status")
                ));
            }
        }
        return bookings;
    }

    public Booking createBooking(Booking booking) throws SQLException {
        validateRoomAvailability(booking.roomId());

        String sql = "INSERT INTO hotel_bookings(guest_name, guest_email, room_id, check_in_date, check_out_date, booking_status) VALUES(?,?,?,?,?,?)";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, booking.guestName());
            statement.setString(2, booking.guestEmail());
            statement.setInt(3, booking.roomId());
            statement.setDate(4, Date.valueOf(booking.checkInDate()));
            statement.setDate(5, Date.valueOf(booking.checkOutDate()));
            statement.setString(6, booking.bookingStatus());
            statement.executeUpdate();

            markRoomUnavailable(booking.roomId());

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return new Booking(
                            keys.getInt(1),
                            booking.guestName(),
                            booking.guestEmail(),
                            booking.roomId(),
                            booking.checkInDate(),
                            booking.checkOutDate(),
                            booking.bookingStatus());
                }
            }
        }
        throw new SQLException("Unable to create booking");
    }

    private void validateRoomAvailability(int roomId) throws SQLException {
        String sql = "SELECT is_available FROM hotel_rooms WHERE id = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, roomId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Room does not exist: " + roomId);
                }
                if (!resultSet.getBoolean("is_available")) {
                    throw new SQLException("Room is not available: " + roomId);
                }
            }
        }
    }

    private void markRoomUnavailable(int roomId) throws SQLException {
        String sql = "UPDATE hotel_rooms SET is_available = FALSE WHERE id = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, roomId);
            statement.executeUpdate();
        }
    }

    public static Booking parseBookingPayload(String guestName,
                                              String guestEmail,
                                              int roomId,
                                              String checkIn,
                                              String checkOut,
                                              String bookingStatus) {
        return new Booking(
                null,
                guestName,
                guestEmail,
                roomId,
                LocalDate.parse(checkIn),
                LocalDate.parse(checkOut),
                bookingStatus);
    }
}
