
package com.highschoolportal.ui.managerpages;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.highschoolportal.app.App;
import com.highschoolportal.model.Manager;
import com.highschoolportal.model.Manager.SuperManagerStatus;
import com.highschoolportal.services.SchoolAdministrationService;
import com.highschoolportal.services.SuperManagerService;
import com.highschoolportal.ui.PageNames;

public class ManagerHomePageUI extends JPanel {

    private final App app;

    private JButton schoolRegButton;
    private JButton academicYearButton;
    private JButton subjectManagementButton;
    private JButton passwordRequestsButton;
    private JButton superManagerButton;
    private JButton logoutButton;
    private JButton deleteSchoolButton;

    private final SchoolAdministrationService schoolAdministrationService =
            new SchoolAdministrationService();

    public ManagerHomePageUI(App app) {
        this.app = app;
        initializeComponents();
    }

    public void initializeComponents() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JPanel up = new JPanel(new BorderLayout());
        JLabel heading = new JLabel("School Manager HomePage");
        heading.setFont(heading.getFont().deriveFont(22f));
        up.add(heading, BorderLayout.CENTER);
        add(up, BorderLayout.NORTH);

        JPanel pages = new JPanel(new FlowLayout());

        superManagerButton = new JButton("Super Manager Status");
        superManagerButton.addActionListener(event -> {
            Manager manager = (Manager) app.getCurrentUser();
            SuperManagerStatus superManagerStatus =
                    manager.getSuperManagerStatus();

            if (superManagerStatus == SuperManagerStatus.NONE) {
                if (showConfirmationPrompt(
                        "Are you sure you would like to request the super manager status?",
                        "Confirm Super Manager Request") == JOptionPane.YES_OPTION) {

                    if (new SuperManagerService().requestSuperStatus(manager)) {
                        superManagerButton.setVisible(false);
                        JOptionPane.showMessageDialog(
                                this,
                                "Super Manager request submitted\n"
                                + "Your patience is appreciated as we review your request"
                        );
                    } else {
                        showError("Super Manager request failed.\nPlease try again later.");
                    }
                }
            } else if (superManagerStatus == SuperManagerStatus.PENDING) {
                JOptionPane.showMessageDialog(
                        this,
                        "Hey there!\n"
                        + "We are reviewing your super manager request at the moment.\n"
                        + "You will notice a different message on this button based on whether we approve or not."
                );
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Ladies & Gents!\n"
                        + "We've got our super manager on board at the moment.\n"
                        + "You do not need to press on this button anymore...it's just there to remind you of how cool you are :)"
                );
            }
        });

        schoolRegButton = new JButton("School Management");
        academicYearButton = new JButton("Academic Year Management");
        subjectManagementButton = new JButton("Subject Management");

        JButton teacherManagementButton =
                new JButton("Teacher Management");

        passwordRequestsButton =
                new JButton("Password Reset Requests");

        deleteSchoolButton = new JButton("Delete My School");
        deleteSchoolButton.setBackground(new Color(220, 70, 70));
        deleteSchoolButton.setForeground(Color.WHITE);

        logoutButton = new JButton("Logout");
        logoutButton.setBackground(Color.RED);

        pages.add(superManagerButton);
        pages.add(schoolRegButton);
        pages.add(academicYearButton);
        pages.add(subjectManagementButton);
        pages.add(teacherManagementButton);
        pages.add(passwordRequestsButton);
        pages.add(deleteSchoolButton);
        pages.add(logoutButton);

        add(pages, BorderLayout.CENTER);

        schoolRegButton.addActionListener(event
                -> this.app.showPage(PageNames.MANAGER_SCHOOL_SIGNUP)
        );

        academicYearButton.addActionListener(event
                -> this.app.showPage(PageNames.MANAGER_ACADEMIC_YEAR_MANAGEMENT)
        );

        subjectManagementButton.addActionListener(event
                -> this.app.showPage(PageNames.MANAGER_SUBJECT_MANAGEMENT)
        );

        passwordRequestsButton.addActionListener(event
                -> this.app.showPage(PageNames.MANAGER_PASSWORD_MANAGEMENT)
        );

        teacherManagementButton.addActionListener(event
                -> app.showPage(PageNames.MANAGER_TEACHER_MANAGEMENT)
        );

        deleteSchoolButton.addActionListener(event -> deleteMySchool());

        logoutButton.addActionListener(event -> app.logout());

        updateSchoolButtons();
    }

    private void deleteMySchool() {
        Manager manager = (Manager) app.getCurrentUser();

        if (manager == null || manager.getSchoolID() <= 0) {
            showError("You are not currently assigned to a school.");
            updateSchoolButtons();
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "WARNING: This action cannot be undone.\n\n"
                + "Deleting your school will permanently delete its "
                + "students, teachers, academic years, subjects, "
                + "enrolments and associated records.\n\n"
                + "Your Manager account and login credentials will remain.\n\n"
                + "Are you sure you want to continue?",
                "Confirm School Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        int finalConfirmation = JOptionPane.showConfirmDialog(
                this,
                "Please confirm one final time that you want to delete your school.",
                "Final Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (finalConfirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean deleted =
                    schoolAdministrationService
                            .deleteSchoolAndRetainManager(manager);

            if (deleted) {
                JOptionPane.showMessageDialog(
                        this,
                        "Your school has been deleted successfully.\n"
                        + "Your Manager account has been retained."
                );

                updateSchoolButtons();
                app.showPage(PageNames.MANAGER_SCHOOL_SIGNUP);
            } else {
                showError("The school could not be deleted. "
                        + "Please refresh and try again.");
            }

        } catch (Exception exception) {
            exception.printStackTrace();
            showError(
                    "School deletion failed.\n"
                    + "No changes should have been committed.\n\n"
                    + "Please check the database connection and schema."
            );
        }
    }

    private void updateSchoolButtons() {
        Manager manager = (Manager) app.getCurrentUser();

        boolean hasSchool =
                manager != null && manager.getSchoolID() > 0;

        deleteSchoolButton.setEnabled(hasSchool);
        schoolRegButton.setEnabled(!hasSchool);
        academicYearButton.setEnabled(hasSchool);
        subjectManagementButton.setEnabled(hasSchool);
    }

    private int showConfirmationPrompt(String message, String title) {
        return JOptionPane.showConfirmDialog(
                this, message, title, JOptionPane.YES_NO_OPTION
        );
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this, message, "Manager Homepage", JOptionPane.ERROR_MESSAGE
        );
    }
}
