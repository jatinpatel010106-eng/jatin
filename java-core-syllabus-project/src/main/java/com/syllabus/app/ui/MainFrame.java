package com.syllabus.app.ui;

import com.syllabus.app.model.StudentRecord;
import com.syllabus.app.network.HttpPingClient;
import com.syllabus.app.service.StudentService;
import com.syllabus.app.util.FileManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MainFrame extends JFrame {
    private final StudentService service;
    private final DefaultTableModel tableModel;

    private final JTextField idField = new JTextField();
    private final JTextField nameField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JTextField moduleField = new JTextField();
    private final JTextField scoreField = new JTextField();
    private final JTextArea analyticsArea = new JTextArea(8, 32);

    public MainFrame(StudentService service) {
        this.service = service;
        setTitle("Core Java Syllabus Manager (Swing + MySQL)");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        BackgroundPanel panel = new BackgroundPanel("/images/syllabus-bg.png");
        panel.setLayout(new BorderLayout(12, 12));
        setContentPane(panel);

        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Email", "Module", "Score", "Created"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        panel.add(createTopBar(), BorderLayout.NORTH);
        panel.add(createCenterPanel(), BorderLayout.CENTER);
        panel.add(createBottomPanel(), BorderLayout.SOUTH);

        refreshTable();
    }

    private JPanel createTopBar() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);

        JButton refreshBtn = new JButton("Refresh Records");
        refreshBtn.addActionListener(e -> refreshTable());

        JButton exportBtn = new JButton("Export CSV");
        exportBtn.addActionListener(e -> {
            try {
                FileManager.exportCsv(service.listAll(), Path.of("exports", "records.csv"));
                showMessage("CSV exported to exports/records.csv");
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        top.add(refreshBtn);
        top.add(exportBtn);
        return top;
    }

    private JSplitPane createCenterPanel() {
        JTable table = new JTable(tableModel);
        JScrollPane tablePane = new JScrollPane(table);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("CRUD Form", buildFormPanel());
        tabs.addTab("Analytics", buildAnalyticsPanel());
        tabs.addTab("Networking", buildNetworkingPanel());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tablePane, tabs);
        splitPane.setResizeWeight(0.55);
        return splitPane;
    }

    private JPanel buildFormPanel() {
        JPanel form = new JPanel(new GridLayout(7, 2, 8, 8));
        form.add(new JLabel("ID (update/delete):"));
        form.add(idField);
        form.add(new JLabel("Name:"));
        form.add(nameField);
        form.add(new JLabel("Email:"));
        form.add(emailField);
        form.add(new JLabel("Module:"));
        form.add(moduleField);
        form.add(new JLabel("Score:"));
        form.add(scoreField);

        JButton createBtn = new JButton("Create");
        createBtn.addActionListener(e -> performCreate());
        JButton updateBtn = new JButton("Update");
        updateBtn.addActionListener(e -> performUpdate());
        JButton deleteBtn = new JButton("Delete");
        deleteBtn.addActionListener(e -> performDelete());

        form.add(createBtn);
        form.add(updateBtn);
        form.add(deleteBtn);
        form.add(new JLabel());
        return wrap(form);
    }

    private JPanel buildAnalyticsPanel() {
        analyticsArea.setEditable(false);
        JButton analyzeBtn = new JButton("Run Stream Analytics");
        analyzeBtn.addActionListener(e -> {
            List<StudentRecord> records = service.listAll();
            Map<String, Double> avgMap = service.averageScoreByModule(records);
            Optional<StudentRecord> top = service.topScorer(records);

            StringBuilder out = new StringBuilder("Average by module:\n");
            avgMap.forEach((module, avg) -> out.append(module).append(" -> ").append(String.format("%.2f", avg)).append('\n'));
            out.append("\nTop scorer: ").append(top.map(StudentRecord::getName).orElse("N/A"));
            analyticsArea.setText(out.toString());
        });

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.add(analyzeBtn, BorderLayout.NORTH);
        panel.add(new JScrollPane(analyticsArea), BorderLayout.CENTER);
        return wrap(panel);
    }

    private JPanel buildNetworkingPanel() {
        JTextField urlField = new JTextField("https://example.com");
        JTextArea responseArea = new JTextArea(12, 30);
        responseArea.setEditable(false);

        JButton fetchBtn = new JButton("Fetch via HttpURLConnection");
        fetchBtn.addActionListener(e -> {
            try {
                responseArea.setText(HttpPingClient.fetch(urlField.getText().trim()));
            } catch (Exception ex) {
                responseArea.setText("Failed: " + ex.getMessage());
            }
        });

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.add(urlField, BorderLayout.NORTH);
        panel.add(new JScrollPane(responseArea), BorderLayout.CENTER);
        panel.add(fetchBtn, BorderLayout.SOUTH);
        return wrap(panel);
    }

    private JPanel createBottomPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        footer.add(new JLabel("Covers OOP, JDBC, Streams, NIO, Serialization, Multithreading, Swing, Networking"));
        return footer;
    }

    private JPanel wrap(JComponent component) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private void performCreate() {
        try {
            service.create(nameField.getText(), emailField.getText(), moduleField.getText(), Double.parseDouble(scoreField.getText()));
            refreshTable();
            showMessage("Record created");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void performUpdate() {
        try {
            int id = Integer.parseInt(idField.getText());
            service.update(id, nameField.getText(), emailField.getText(), moduleField.getText(), Double.parseDouble(scoreField.getText()));
            refreshTable();
            showMessage("Record updated");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void performDelete() {
        try {
            int id = Integer.parseInt(idField.getText());
            service.delete(id);
            refreshTable();
            showMessage("Record deleted");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (StudentRecord record : service.listAll()) {
            tableModel.addRow(new Object[]{
                    record.getId(), record.getName(), record.getEmail(),
                    record.getCourseModule(), record.getScore(), record.getCreatedDate()
            });
        }
    }

    private void showMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
