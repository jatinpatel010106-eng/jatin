package com.syllabus.app.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class StudentRecord implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String name;
    private String email;
    private String courseModule;
    private double score;
    private LocalDate createdDate;

    public StudentRecord() {
    }

    public StudentRecord(Integer id, String name, String email, String courseModule, double score, LocalDate createdDate) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.courseModule = courseModule;
        this.score = score;
        this.createdDate = createdDate;
    }

    public StudentRecord(String name, String email, String courseModule, double score, LocalDate createdDate) {
        this(null, name, email, courseModule, score, createdDate);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCourseModule() {
        return courseModule;
    }

    public void setCourseModule(String courseModule) {
        this.courseModule = courseModule;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StudentRecord that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }
}
