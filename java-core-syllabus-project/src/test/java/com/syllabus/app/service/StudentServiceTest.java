package com.syllabus.app.service;

import com.syllabus.app.dao.StudentRecordDao;
import com.syllabus.app.model.StudentRecord;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class StudentServiceTest {

    @Test
    void shouldCalculateAnalytics() {
        StudentService service = new StudentService(new InMemoryDao());
        List<StudentRecord> records = List.of(
                new StudentRecord(1, "A", "a@x.com", "OOP", 70, LocalDate.now()),
                new StudentRecord(2, "B", "b@x.com", "OOP", 90, LocalDate.now()),
                new StudentRecord(3, "C", "c@x.com", "NIO", 88, LocalDate.now())
        );

        Map<String, Double> avg = service.averageScoreByModule(records);
        Optional<StudentRecord> top = service.topScorer(records);

        assertEquals(80.0, avg.get("OOP"));
        assertEquals("B", top.orElseThrow().getName());
    }

    @Test
    void shouldValidateInput() {
        StudentService service = new StudentService(new InMemoryDao());
        assertThrows(ValidationException.class, () -> service.create("", "bad", "", 101));
    }

    private static class InMemoryDao implements StudentRecordDao {
        List<StudentRecord> storage = new ArrayList<>();

        @Override
        public StudentRecord save(StudentRecord record) {
            storage.add(record);
            return record;
        }

        @Override
        public List<StudentRecord> findAll() {
            return storage;
        }

        @Override
        public Optional<StudentRecord> findById(int id) {
            return storage.stream().filter(x -> x.getId() != null && x.getId() == id).findFirst();
        }

        @Override
        public void update(StudentRecord record) {
        }

        @Override
        public void delete(int id) {
        }
    }
}
