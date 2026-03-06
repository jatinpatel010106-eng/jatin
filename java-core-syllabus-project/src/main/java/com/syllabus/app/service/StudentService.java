package com.syllabus.app.service;

import com.syllabus.app.dao.StudentRecordDao;
import com.syllabus.app.model.StudentRecord;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class StudentService {
    private final StudentRecordDao dao;

    public StudentService(StudentRecordDao dao) {
        this.dao = dao;
    }

    public StudentRecord create(String name, String email, String module, double score) {
        validate(name, email, module, score);
        StudentRecord record = new StudentRecord(name.trim(), email.trim().toLowerCase(), module.trim(), score, LocalDate.now());
        return dao.save(record);
    }

    public List<StudentRecord> listAll() {
        return dao.findAll();
    }

    public void update(int id, String name, String email, String module, double score) {
        validate(name, email, module, score);
        StudentRecord record = new StudentRecord(id, name.trim(), email.trim().toLowerCase(), module.trim(), score, LocalDate.now());
        dao.update(record);
    }

    public void delete(int id) {
        dao.delete(id);
    }

    public Map<String, Double> averageScoreByModule(List<StudentRecord> records) {
        return records.stream().collect(Collectors.groupingBy(
                StudentRecord::getCourseModule,
                LinkedHashMap::new,
                Collectors.averagingDouble(StudentRecord::getScore)
        ));
    }

    public Optional<StudentRecord> topScorer(List<StudentRecord> records) {
        return records.stream().max(Comparator.comparingDouble(StudentRecord::getScore));
    }

    private void validate(String name, String email, String module, double score) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Name cannot be empty");
        }
        if (email == null || !email.contains("@")) {
            throw new ValidationException("Email is invalid");
        }
        if (module == null || module.isBlank()) {
            throw new ValidationException("Module cannot be empty");
        }
        if (score < 0 || score > 100) {
            throw new ValidationException("Score must be between 0 and 100");
        }
    }
}
