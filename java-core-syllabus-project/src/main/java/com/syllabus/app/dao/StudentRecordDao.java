package com.syllabus.app.dao;

import com.syllabus.app.model.StudentRecord;

import java.util.List;
import java.util.Optional;

public interface StudentRecordDao {
    StudentRecord save(StudentRecord record);

    List<StudentRecord> findAll();

    Optional<StudentRecord> findById(int id);

    void update(StudentRecord record);

    void delete(int id);
}
