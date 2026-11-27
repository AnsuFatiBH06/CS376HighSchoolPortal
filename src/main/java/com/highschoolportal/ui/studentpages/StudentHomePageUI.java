
package com.highschoolportal.ui.studentpages;

import com.highschoolportal.app.App;
import com.highschoolportal.model.User;
import com.highschoolportal.services.StudentManagementService;
import com.highschoolportal.ui.PageNames;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.Map;

public class StudentHomePageUI extends JPanel {

    private final App app;
    private final StudentManagementService studentService;

    private final JComboBox<AcademicYearOption> academicYearCombo;
    private final JLabel statusLabel;

    private boolean loadingYears = false;

    public StudentHomePageUI(App app) {
        this.app = app;
        this.studentService = new StudentManagementService();

        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel heading = new JLabel("Student Homepage");
        heading.setFont(heading.getFont().deriveFont(22f));
        add(heading, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel qualificationHeading =
                new JLabel("Academic Qualification Status");
        qualificationHeading.setFont(
                qualificationHeading.getFont().deriveFont(Font.BOLD, 16f)
        );

        academicYearCombo = new JComboBox<>();
        academicYearCombo.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 32)
        );

        statusLabel = new JLabel("Load your academic years to view your status.");
        statusLabel.setBorder(new EmptyBorder(8, 0, 8, 0));

        JButton refreshQualification =
                new JButton("Load / Refresh Qualification Status");

        refreshQualification.addActionListener(
                event -> loadAcademicYears()
        );

        academicYearCombo.addActionListener(event -> {
            if (!loadingYears) {
                updateQualificationStatus();
            }
        });

        content.add(qualificationHeading);
        content.add(Box.createVerticalStrut(12));
        content.add(new JLabel("Academic Year:"));
        content.add(academicYearCombo);
        content.add(Box.createVerticalStrut(8));
        content.add(statusLabel);
        content.add(Box.createVerticalStrut(8));
        content.add(refreshQualification);

        add(content, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton update = new JButton("Update Password");
        JButton request = new JButton("Request Password Reset");

        actions.add(update);
        actions.add(request);

        add(actions, BorderLayout.SOUTH);

        update.addActionListener(
                event -> app.showPage(PageNames.UPDATE_PASSWORD)
        );

        request.addActionListener(
                event -> app.showPage(PageNames.PASSWORD_RESET_REQUEST)
        );
    }

    private void loadAcademicYears() {
        User currentUser = app.getCurrentUser();

        if (currentUser == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please log in before viewing your qualification status.",
                    "Login Required",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            Map<Integer, String> academicYears =
                    studentService.getStudentAcademicYears(
                            currentUser.getUserID()
                    );

            loadingYears = true;
            academicYearCombo.removeAllItems();

            for (Map.Entry<Integer, String> entry : academicYears.entrySet()) {
                academicYearCombo.addItem(
                        new AcademicYearOption(
                                entry.getKey(),
                                entry.getValue()
                        )
                );
            }

            loadingYears = false;

            if (academicYears.isEmpty()) {
                statusLabel.setText(
                        "No enrolled academic years were found."
                );
                return;
            }

            updateQualificationStatus();

        } catch (SQLException | IllegalArgumentException exception) {
            loadingYears = false;

            JOptionPane.showMessageDialog(
                    this,
                    "Could not load academic years: "
                            + exception.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void updateQualificationStatus() {
        User currentUser = app.getCurrentUser();
        AcademicYearOption selectedYear =
                (AcademicYearOption) academicYearCombo.getSelectedItem();

        if (currentUser == null || selectedYear == null) {
            return;
        }

        try {
            String status =
                    studentService.getQualificationStatusForStudent(
                            currentUser.getUserID(),
                            selectedYear.getAcademicYearID()
                    );

            statusLabel.setText("Qualification Status: " + status);

        } catch (SQLException | IllegalArgumentException exception) {
            statusLabel.setText("Unable to retrieve qualification status.");

            JOptionPane.showMessageDialog(
                    this,
                    "Could not retrieve qualification status: "
                            + exception.getMessage(),
                    "Database Error",
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
