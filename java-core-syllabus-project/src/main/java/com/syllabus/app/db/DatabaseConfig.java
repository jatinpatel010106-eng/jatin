package com.syllabus.app.db;

public final class DatabaseConfig {
    private DatabaseConfig() {
    }

    public static String url() {
        return System.getenv().getOrDefault("JAVA_DB_URL", "jdbc:mysql://localhost:3306/java_syllabus_db");
    }

    public static String user() {
        return System.getenv().getOrDefault("JAVA_DB_USER", "root");
    }

    public static String password() {
        return System.getenv().getOrDefault("JAVA_DB_PASSWORD", "root");
    }
}
