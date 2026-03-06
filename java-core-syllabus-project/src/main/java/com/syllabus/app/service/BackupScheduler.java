package com.syllabus.app.service;

import com.syllabus.app.util.FileManager;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BackupScheduler {
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final StudentService service;

    public BackupScheduler(StudentService service) {
        this.service = service;
    }

    public void start() {
        executorService.scheduleAtFixedRate(() -> {
            try {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                Path output = Path.of("exports", "backup_" + timestamp + ".ser");
                FileManager.serialize(service.listAll(), output);
            } catch (Exception ignored) {
                // Intentionally ignored to keep scheduled task resilient for classroom demo.
            }
        }, 30, 60, TimeUnit.SECONDS);
    }

    public void stop() {
        executorService.shutdownNow();
    }
}
