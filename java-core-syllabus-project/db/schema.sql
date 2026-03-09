CREATE DATABASE IF NOT EXISTS java_syllabus_db;
USE java_syllabus_db;

CREATE TABLE IF NOT EXISTS student_records (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    course_module VARCHAR(80) NOT NULL,
    score DOUBLE NOT NULL,
    created_date DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS hotel_rooms (
    id INT PRIMARY KEY AUTO_INCREMENT,
    room_number VARCHAR(20) NOT NULL UNIQUE,
    room_type VARCHAR(40) NOT NULL,
    price_per_night DECIMAL(10,2) NOT NULL,
    is_available BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS hotel_bookings (
    id INT PRIMARY KEY AUTO_INCREMENT,
    guest_name VARCHAR(100) NOT NULL,
    guest_email VARCHAR(120) NOT NULL,
    room_id INT NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    booking_status VARCHAR(30) NOT NULL DEFAULT 'CONFIRMED',
    CONSTRAINT fk_hotel_bookings_room FOREIGN KEY (room_id) REFERENCES hotel_rooms(id)
);
