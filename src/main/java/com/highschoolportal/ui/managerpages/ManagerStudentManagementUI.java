
package com.highschoolportal.ui.managerpages;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.highschoolportal.app.App;
import com.highschoolportal.model.IncompleteCurriculumStudent;
import com.highschoolportal.model.StudentSummary;
import com.highschoolportal.services.StudentManagementService;

public class ManagerStudentManagementUI extends JPanel {

    private final App app;
    private final StudentManagementService service =
            new StudentManagementService();

    private final JComboBox<String> subjectCodeBox = new JComboBox<>();
    private final JComboBox<String> certificateStatusBox =
            new JComboBox<>(
                    new String[]{"All", "Qualified", "Unqualified",
                        "Results Processing"});

    private final JCheckBox incompleteCurriculumBox =
            new JCheckBox("Incomplete curriculum only");

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{
                "Student ID", "Student Name", "Email",
                "Academic Year", "Certificate Status"
            }, 0) {

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(model);

    public ManagerStudentManagementUI(App app) {
        this.app = app;

        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel top = new JPanel(new BorderLayout());

        JButton backButton = new JButton("Back to Homepage");
        JLabel heading = new JLabel("Student Management");
        heading.setFont(heading.getFont().deriveFont(22f));

        top.add(backButton, BorderLayout.WEST);
        top.add(heading, BorderLayout.SOUTH);

        backButton.addActionListener(event -> app.goBack());

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));

        filters.add(new JLabel("Subject code:"));
        filters.add(subjectCodeBox);

        filters.add(new JLabel("Certificate status:"));
        filters.add(certificateStatusBox);

        filters.add(incompleteCurriculumBox);

        JButton applyFiltersButton = new JButton("Apply Filters");
        JButton clearFiltersButton = new JButton("Clear Filters");

        filters.add(applyFiltersButton);
        filters.add(clearFiltersButton);

        JPanel header = new JPanel(new BorderLayout(0, 12));
        header.add(top, BorderLayout.NORTH);
        header.add(filters, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton refreshButton = new JButton("Refresh Subjects");
        JButton notifyButton = new JButton("Notify Selected Student");
        JButton qualificationButton =
                new JButton("View Qualification Status");

        actions.add(refreshButton);
        actions.add(notifyButton);
        actions.add(qualificationButton);

        add(actions, BorderLayout.SOUTH);

        applyFiltersButton.addActionListener(event -> applyFilters());
        clearFiltersButton.addActionListener(event -> clearFilters());
        refreshButton.addActionListener(event -> loadSubjectCodes());
        notifyButton.addActionListener(event -> notifySelectedStudent());
        qualificationButton.addActionListener(
                event -> viewQualificationStatus()
        );

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(
                    java.awt.event.ComponentEvent event) {

                if (app.getCurrentUser()
                        instanceof com.highschoolportal.model.Manager) {

                    model.setRowCount(0);
                    certificateStatusBox.setSelectedIndex(0);
                    incompleteCurriculumBox.setSelected(false);

                    loadSubjectCodes();
                    showAllStudents();
                }
            }
        });
    }

    private int selectedStudentID() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow < 0) {
            return -1;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);

        return (Integer) model.getValueAt(modelRow, 0);
    }

    private void loadSubjectCodes() {
        try {
            int managerID = app.getCurrentUser().getUserID();

            subjectCodeBox.removeAllItems();
            subjectCodeBox.addItem("All");

            for (String subjectCode : service.getSubjectCodes(managerID)) {
                subjectCodeBox.addItem(subjectCode);
            }

        } catch (Exception exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getMessage(),
                    "Load Subjects",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void showAllStudents() {
        try {
            int managerID = app.getCurrentUser().getUserID();

            model.setRowCount(0);

            List<StudentSummary> students =
                    service.getAllStudents(managerID);

            for (StudentSummary student : students) {
                model.addRow(new Object[]{
                    student.getStudentID(),
                    student.getStudentName(),
                    student.getEmail(),
                    student.getAcademicYearName(),
                    student.getCertificateStatus()
                });
            }

            if (students.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "No students were found in your school.",
                        "Student Management",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

        } catch (Exception exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getMessage(),
                    "Student Management",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void applyFilters() {
        try {
            int managerID = app.getCurrentUser().getUserID();

            String selectedSubjectCode =
                    (String) subjectCodeBox.getSelectedItem();

            String selectedCertificateStatus =
                    (String) certificateStatusBox.getSelectedItem();

            String subjectCode = "All".equals(selectedSubjectCode)
                    ? ""
                    : selectedSubjectCode;

            String certificateStatus =
                    "All".equals(selectedCertificateStatus)
                            ? ""
                            : selectedCertificateStatus;

            model.setRowCount(0);

            if (incompleteCurriculumBox.isSelected()) {
                List<IncompleteCurriculumStudent> students =
                        service.getStudentsWithIncompleteCurricula(
                                managerID
                        );

                for (IncompleteCurriculumStudent student : students) {
                    model.addRow(new Object[]{
                        student.getStudentID(),
                        student.getStudentName(),
                        student.getEmail(),
                        student.getAcademicYearName(),
                        student.getOptionalSubjectsSelected() + " / "
                        + student.getMinimumOptionalSubjects()
                    });
                }

                if (students.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            this,
                            "All students have complete curricula.",
                            "Student Management",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }

                return;
            }

            List<StudentSummary> students =
                    service.filterStudents(
                            managerID,
                            subjectCode,
                            certificateStatus
                    );

            for (StudentSummary student : students) {
                model.addRow(new Object[]{
                    student.getStudentID(),
                    student.getStudentName(),
                    student.getEmail(),
                    student.getAcademicYearName(),
                    student.getCertificateStatus()
                });
            }

            if (students.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "No students were found for the selected filters.",
                        "Student Management",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

        } catch (Exception exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getMessage(),
                    "Student Management",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void clearFilters() {
        subjectCodeBox.setSelectedIndex(0);
        certificateStatusBox.setSelectedIndex(0);
        incompleteCurriculumBox.setSelected(false);

        showAllStudents();
    }

    private void notifySelectedStudent() {
        int studentID = selectedStudentID();

        if (studentID < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Select a student first.",
                    "Notify Student",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String message = JOptionPane.showInputDialog(
                this,
                "Enter the notification message:",
                "Notify Student",
                JOptionPane.QUESTION_MESSAGE
        );

        if (message == null || message.isBlank()) {
            return;
        }

        try {
            int managerID = app.getCurrentUser().getUserID();

            service.notifyStudent(managerID, studentID, message);

            JOptionPane.showMessageDialog(
                    this,
                    "Notification sent successfully."
            );

        } catch (Exception exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getMessage(),
                    "Notify Student",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void viewQualificationStatus() {
        int studentID = selectedStudentID();

        if (studentID < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Select a student first.",
                    "Qualification Status",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            int managerID = app.getCurrentUser().getUserID();

            Map<Integer, String> academicYears =
                    service.getStudentAcademicYears(studentID);

            if (academicYears.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "No academic years were found for this student.",
                        "Qualification Status",
                        JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }

            JComboBox<AcademicYearOption> yearBox = new JComboBox<>();

            for (Map.Entry<Integer, String> entry
                    : academicYears.entrySet()) {

                yearBox.addItem(
                        new AcademicYearOption(
                                entry.getKey(),
                                entry.getValue()
                        )
                );
            }

            int choice = JOptionPane.showConfirmDialog(
                    this,
                    yearBox,
                    "Select Academic Year",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (choice != JOptionPane.OK_OPTION) {
                return;
            }

            AcademicYearOption selectedYear =
                    (AcademicYearOption) yearBox.getSelectedItem();

            if (selectedYear == null) {
                return;
            }

            String status =
                    service.getQualificationStatusForManager(
                            managerID,
                            studentID,
                            selectedYear.getAcademicYearID()
                    );

            JOptionPane.showMessageDialog(
                    this,
                    "Qualification Status: " + status,
                    "Student Qualification Status",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getMessage(),
                    "Qualification Status",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private static class AcademicYearOption {

        private final int academicYearID;
        private final String name;

        public AcademicYearOption(int academicYearID, String name) {
            this.academicYearID = academicYearID;
            this.name = name;
        }

        public int getAcademicYearID() {
            return academicYearID;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
