package com.syllabus.app.dao;

import com.syllabus.app.db.DatabaseManager;
import com.syllabus.app.model.StudentRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MySqlStudentRecordDao implements StudentRecordDao {
    private final DatabaseManager databaseManager;

    public MySqlStudentRecordDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public StudentRecord save(StudentRecord record) {
        String sql = "INSERT INTO student_records(name,email,course_module,score,created_date) VALUES (?,?,?,?,?)";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, record.getName());
            statement.setString(2, record.getEmail());
            statement.setString(3, record.getCourseModule());
            statement.setDouble(4, record.getScore());
            statement.setDate(5, Date.valueOf(record.getCreatedDate()));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    record.setId(keys.getInt(1));
                }
            }
            return record;
        } catch (SQLException e) {
            throw new DataAccessException("Unable to insert student record", e);
        }
    }

    @Override
    public List<StudentRecord> findAll() {
        String sql = "SELECT id,name,email,course_module,score,created_date FROM student_records ORDER BY id DESC";
        List<StudentRecord> records = new ArrayList<>();
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                records.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to list student records", e);
        }
        return records;
    }

    @Override
    public Optional<StudentRecord> findById(int id) {
        String sql = "SELECT id,name,email,course_module,score,created_date FROM student_records WHERE id = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to fetch student record", e);
        }
    }

    @Override
    public void update(StudentRecord record) {
        String sql = "UPDATE student_records SET name=?, email=?, course_module=?, score=? WHERE id=?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, record.getName());
            statement.setString(2, record.getEmail());
            statement.setString(3, record.getCourseModule());
            statement.setDouble(4, record.getScore());
            statement.setInt(5, record.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to update student record", e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM student_records WHERE id = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to delete student record", e);
        }
    }

    private StudentRecord mapRow(ResultSet rs) throws SQLException {
        return new StudentRecord(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("course_module"),
                rs.getDouble("score"),
                rs.getDate("created_date").toLocalDate()
        );
    }
}
