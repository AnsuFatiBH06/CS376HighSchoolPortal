package com.highschoolportal.ui.studentpages;

import com.highschoolportal.app.App;
import com.highschoolportal.model.StudentTeachingMaterial;
import com.highschoolportal.services.TeachingService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TeachingMaterialsUI extends JPanel {

    private final App app;
    private final TeachingService service = new TeachingService();

    private final JComboBox<String> subjectBox = new JComboBox<>();

    private Map<Integer, String> subjectOfferings = new LinkedHashMap<>();

    private final DefaultTableModel model = new DefaultTableModel(new Object[]{
            "Material ID", "Week", "Subject", "Teacher",
            "Uploaded At", "File"
    }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(model);

    public TeachingMaterialsUI(App app) {
        this.app = app;

        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel top = new JPanel(new BorderLayout());

        JButton backButton = new JButton("Back to Homepage");
        JLabel heading = new JLabel("Weekly Teaching Materials");
        heading.setFont(heading.getFont().deriveFont(22f));

        top.add(backButton, BorderLayout.WEST);
        top.add(heading, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        backButton.addActionListener(event -> app.goBack());

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));

        filters.add(new JLabel("Subject:"));
        filters.add(subjectBox);

        JButton viewButton = new JButton("View Materials");
        filters.add(viewButton);

        add(filters, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton refreshButton = new JButton("Refresh");
        JButton downloadButton = new JButton("Download Selected Material");

        actions.add(refreshButton);
        actions.add(downloadButton);

        add(actions, BorderLayout.SOUTH);

        viewButton.addActionListener(event -> viewMaterials());
        refreshButton.addActionListener(event -> loadSubjects());
        downloadButton.addActionListener(event -> downloadSelectedMaterial());

        loadSubjects();
    }

    private void loadSubjects() {
        try {
            int studentID = app.getCurrentUser().getUserID();

            subjectBox.removeAllItems();

            subjectOfferings = service.getStudentSubjectOfferings(studentID);

            subjectBox.addItem("Select a subject");

            for (String subjectDisplay : subjectOfferings.values()) {
                subjectBox.addItem(subjectDisplay);
            }

            if (subjectOfferings.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "You are not enrolled in any subjects.",
                        "Weekly Teaching Materials",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this,
                    exception.getMessage(),
                    "Load Subjects",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private int selectedSubjectAcademicYearID() {
        String selectedSubject = (String) subjectBox.getSelectedItem();

        if (selectedSubject == null
                || selectedSubject.equals("Select a subject")) {
            return 0;
        }

        for (Map.Entry<Integer, String> offering : subjectOfferings.entrySet()) {
            if (offering.getValue().equals(selectedSubject)) {
                return offering.getKey();
            }
        }

        return 0;
    }

    private void viewMaterials() {
        int subjectAcademicYearID = selectedSubjectAcademicYearID();

        if (subjectAcademicYearID <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Select a subject first.",
                    "Weekly Teaching Materials",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int studentID = app.getCurrentUser().getUserID();

            model.setRowCount(0);

            List<StudentTeachingMaterial> materials =
                    service.getStudentTeachingMaterials(
                            studentID,
                            subjectAcademicYearID
                    );

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            for (StudentTeachingMaterial material : materials) {
                model.addRow(new Object[]{
                        material.getMaterialID(),
                        material.getWeekNumber(),
                        material.getSubjectName(),
                        material.getTeacherName(),
                        material.getUploadDate() == null
                                ? ""
                                : material.getUploadDate().format(formatter),
                        new File(material.getFilePath()).getName()
                });
            }

            if (materials.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No teaching materials were found for this subject.",
                        "Weekly Teaching Materials",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this,
                    exception.getMessage(),
                    "Weekly Teaching Materials",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void downloadSelectedMaterial() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Select a teaching material first.",
                    "Download Material",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        int materialID = (Integer) model.getValueAt(modelRow, 0);

        try {
            int studentID = app.getCurrentUser().getUserID();

            File downloadedFile = service.downloadTeachingMaterial(
                    studentID,
                    materialID
            );

            JOptionPane.showMessageDialog(this,
                    "Teaching material downloaded successfully.\n"
                            + downloadedFile.getAbsolutePath());

        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this,
                    exception.getMessage(),
                    "Download Material",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}