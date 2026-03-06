package com.syllabus.app.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DatabaseConfig.url(), DatabaseConfig.user(), DatabaseConfig.password());
    }
}
