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
