package com.syllabus.app;

import com.syllabus.app.dao.MySqlStudentRecordDao;
import com.syllabus.app.db.DatabaseManager;
import com.syllabus.app.service.BackupScheduler;
import com.syllabus.app.service.StudentService;
import com.syllabus.app.ui.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        DatabaseManager databaseManager = new DatabaseManager();
        StudentService service = new StudentService(new MySqlStudentRecordDao(databaseManager));
        BackupScheduler backupScheduler = new BackupScheduler(service);
        backupScheduler.start();

        Runtime.getRuntime().addShutdownHook(new Thread(backupScheduler::stop));

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(service);
            frame.setVisible(true);
        });
    }
}
