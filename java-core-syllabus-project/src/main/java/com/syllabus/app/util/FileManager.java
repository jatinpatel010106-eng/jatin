package com.syllabus.app.util;

import com.syllabus.app.model.StudentRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class FileManager {
    private FileManager() {
    }

    public static void exportCsv(List<StudentRecord> records, Path path) throws IOException {
        Files.createDirectories(path.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write("id,name,email,module,score,createdDate");
            writer.newLine();
            for (StudentRecord record : records) {
                writer.write(String.format("%d,%s,%s,%s,%.2f,%s",
                        record.getId(),
                        record.getName(),
                        record.getEmail(),
                        record.getCourseModule(),
                        record.getScore(),
                        record.getCreatedDate()));
                writer.newLine();
            }
        }
    }

    public static void serialize(List<StudentRecord> records, Path path) throws IOException {
        Files.createDirectories(path.getParent());
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(path))) {
            oos.writeObject(records);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<StudentRecord> deserialize(Path path) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(path))) {
            return (List<StudentRecord>) ois.readObject();
        }
    }
}
