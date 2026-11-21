package com.highschoolportal.ui.managerpages;

import com.highschoolportal.app.App;
import com.highschoolportal.model.Manager;
import com.highschoolportal.repository.TeacherRepository;
import com.highschoolportal.services.TeacherManagementService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ManagerTeacherUI extends JPanel {
    private final App app;
    private final TeacherManagementService service = new TeacherManagementService();
    private final JTextField firstNameField = new JTextField(22);
    private final JTextField lastNameField = new JTextField(22);
    private final JTextField emailField = new JTextField(22);
    private final JTextField dobField = new JTextField(22);
    private final JPasswordField passwordField = new JPasswordField(22);
    private final JPasswordField confirmField = new JPasswordField(22);
    private final JButton createButton = new JButton("Create Teacher");
    private final JComboBox<TeacherRepository.TeacherOption> teacherCombo = new JComboBox<>();
    private final JComboBox<TeacherRepository.SubjectOfferingOption> offeringCombo = new JComboBox<>();
    private final JButton assignButton = new JButton("Assign Teacher");
    private final JButton refreshButton = new JButton("Refresh");
    private final JLabel assignmentStatus = new JLabel("Open this tab to load assignments.");
    private boolean loadingAssignments;
    private boolean assignmentsLoaded;

    public ManagerTeacherUI(App app) {
        this.app = app;
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("Teacher Management");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Create Teacher", buildCreationForm());
        tabs.addTab("Assign Teacher", buildAssignmentForm());
        tabs.addChangeListener(event -> {
            if (tabs.getSelectedIndex() == 1 && !assignmentsLoaded && !loadingAssignments) {
                refreshAssignments();
            }
        });
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildCreationForm() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        addField(form, c, 0, "First name", firstNameField);
        addField(form, c, 1, "Last name", lastNameField);
        addField(form, c, 2, "School email", emailField);
        addField(form, c, 3, "Date of birth (YYYY-MM-DD)", dobField);
        addField(form, c, 4, "Temporary password", passwordField);
        addField(form, c, 5, "Confirm password", confirmField);

        c.gridx = 1;
        c.gridy = 6;
        form.add(createButton, c);
        JButton backButton = new JButton("Back");
        c.gridx = 0;
        form.add(backButton, c);
        createButton.addActionListener(e -> createTeacher());
        backButton.addActionListener(e -> app.goBack());
        return form;
    }

    private JPanel buildAssignmentForm() {
        JPanel panel = new JPanel(new BorderLayout(10, 14));
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        teacherCombo.setPrototypeDisplayValue(
                new TeacherRepository.TeacherOption(0, "A long teacher name (teacher@example.edu)"));
        offeringCombo.setPrototypeDisplayValue(new TeacherRepository.SubjectOfferingOption(
                0, "Academic Year | SUBJECT - Subject title (assigned to #1234)", null));
        addField(form, c, 0, "Teacher", teacherCombo);
        addField(form, c, 1, "Subject and academic year", offeringCombo);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(assignButton);
        controls.add(refreshButton);
        c.gridx = 1;
        c.gridy = 2;
        form.add(controls, c);

        panel.add(form, BorderLayout.NORTH);
        panel.add(assignmentStatus, BorderLayout.CENTER);
        JLabel note = new JLabel("Assignments replace the current teacher for that subject offering.");
        note.setForeground(Color.GRAY);
        panel.add(note, BorderLayout.SOUTH);
        assignButton.setEnabled(false);
        assignButton.addActionListener(e -> assignTeacher());
        refreshButton.addActionListener(e -> refreshAssignments());
        return panel;
    }

    private void refreshAssignments() {
        if (loadingAssignments) return;
        if (!(app.getCurrentUser() instanceof Manager manager)) {
            assignmentStatus.setText("Log in as a school manager to manage assignments.");
            teacherCombo.removeAllItems();
            offeringCombo.removeAllItems();
            assignButton.setEnabled(false);
            assignmentsLoaded = false;
            return;
        }
        loadingAssignments = true;
        refreshButton.setEnabled(false);
        assignButton.setEnabled(false);
        assignmentStatus.setText("Loading teachers and subjects...");
        new SwingWorker<AssignmentOptions, Void>() {
            @Override
            protected AssignmentOptions doInBackground() throws Exception {
                return new AssignmentOptions(service.getTeachers(manager),
                        service.getSubjectOfferings(manager));
            }

            @Override
            protected void done() {
                loadingAssignments = false;
                refreshButton.setEnabled(true);
                try {
                    AssignmentOptions options = get();
                    teacherCombo.removeAllItems();
                    offeringCombo.removeAllItems();
                    for (TeacherRepository.TeacherOption teacher : options.teachers()) {
                        teacherCombo.addItem(teacher);
                    }
                    for (TeacherRepository.SubjectOfferingOption offering : options.offerings()) {
                        offeringCombo.addItem(offering);
                    }
                    assignmentsLoaded = true;
                    assignButton.setEnabled(teacherCombo.getItemCount() > 0
                            && offeringCombo.getItemCount() > 0);
                    assignmentStatus.setText(options.teachers().size() + " active teacher(s), "
                            + options.offerings().size() + " subject offering(s).");
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    assignmentsLoaded = false;
                    assignmentStatus.setText("Loading was interrupted.");
                } catch (ExecutionException exception) {
                    assignmentsLoaded = false;
                    assignmentStatus.setText("Could not load teacher assignments.");
                    showError("Loading assignments failed", exception.getCause());
                }
            }
        }.execute();
    }

    private void assignTeacher() {
        if (!(app.getCurrentUser() instanceof Manager manager)) {
            JOptionPane.showMessageDialog(this, "Only a school manager can assign teachers.");
            return;
        }
        TeacherRepository.TeacherOption teacher =
                (TeacherRepository.TeacherOption) teacherCombo.getSelectedItem();
        TeacherRepository.SubjectOfferingOption offering =
                (TeacherRepository.SubjectOfferingOption) offeringCombo.getSelectedItem();
        if (teacher == null || offering == null) {
            JOptionPane.showMessageDialog(this, "Choose a teacher and subject offering.");
            return;
        }
        Integer previousTeacher = offering.assignedTeacherID();
        if (previousTeacher != null && previousTeacher != teacher.teacherID()) {
            int decision = JOptionPane.showConfirmDialog(this,
                    "This offering is already assigned to teacher #" + previousTeacher
                            + ". Replace that assignment with " + teacher.displayName() + "?",
                    "Confirm reassignment", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (decision != JOptionPane.YES_OPTION) return;
        }
        assignButton.setEnabled(false);
        refreshButton.setEnabled(false);
        assignmentStatus.setText("Saving assignment...");
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                service.assignTeacher(manager, teacher.teacherID(), offering.subjectAcademicYearID());
                return null;
            }

            @Override
            protected void done() {
                refreshButton.setEnabled(true);
                try {
                    get();
                    JOptionPane.showMessageDialog(ManagerTeacherUI.this,
                            "Teacher assignment saved successfully.");
                    assignmentsLoaded = false;
                    refreshAssignments();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    assignmentStatus.setText("Assignment was interrupted.");
                    assignButton.setEnabled(true);
                } catch (ExecutionException exception) {
                    assignmentStatus.setText("Assignment could not be saved.");
                    assignButton.setEnabled(true);
                    showError("Teacher assignment failed", exception.getCause());
                }
            }
        }.execute();
    }

    private void showError(String title, Throwable cause) {
        String message = (cause instanceof IllegalArgumentException || cause instanceof java.sql.SQLException)
                ? cause.getMessage() : "Operation failed. Check the application logs.";
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private record AssignmentOptions(List<TeacherRepository.TeacherOption> teachers,
                                     List<TeacherRepository.SubjectOfferingOption> offerings) {}

    private void addField(JPanel form, GridBagConstraints c, int row,
                          String label, JComponent input) {
        c.gridy = row;
        c.gridx = 0;
        form.add(new JLabel(label), c);
        c.gridx = 1;
        form.add(input, c);
    }

    private void createTeacher() {
        if (!(app.getCurrentUser() instanceof Manager manager)) {
            JOptionPane.showMessageDialog(this, "Only a school manager can create teachers.");
            return;
        }

        final String firstName = firstNameField.getText().trim();
        final String lastName = lastNameField.getText().trim();
        final String email = emailField.getText().trim();
        final LocalDate dob;
        try {
            dob = LocalDate.parse(dobField.getText().trim());
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Date of birth must be YYYY-MM-DD.");
            return;
        }

        char[] password = passwordField.getPassword();
        char[] confirm = confirmField.getPassword();
        if (!Arrays.equals(password, confirm)) {
            Arrays.fill(password, '\0');
            Arrays.fill(confirm, '\0');
            JOptionPane.showMessageDialog(this, "Passwords do not match.");
            return;
        }
        Arrays.fill(confirm, '\0');
        createButton.setEnabled(false);

        new SwingWorker<TeacherRepository.CreatedTeacher, Void>() {
            @Override
            protected TeacherRepository.CreatedTeacher doInBackground() throws Exception {
                try {
                    return service.createTeacher(manager, firstName, lastName, email, dob, password);
                } finally {
                    Arrays.fill(password, '\0');
                }
            }

            @Override
            protected void done() {
                createButton.setEnabled(true);
                passwordField.setText("");
                confirmField.setText("");
                try {
                    TeacherRepository.CreatedTeacher created = get();
                    JOptionPane.showMessageDialog(ManagerTeacherUI.this,
                            "Teacher created successfully (ID " + created.teacherID() + ").");
                    firstNameField.setText("");
                    lastNameField.setText("");
                    emailField.setText("");
                    dobField.setText("");
                    assignmentsLoaded = false;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    JOptionPane.showMessageDialog(ManagerTeacherUI.this,
                            "Teacher creation was interrupted.", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    String message = (cause instanceof IllegalArgumentException || cause instanceof java.sql.SQLException)
                            ? cause.getMessage() : "Teacher creation failed. Check the application logs.";
                    JOptionPane.showMessageDialog(ManagerTeacherUI.this,
                            message, "Could not create teacher", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
