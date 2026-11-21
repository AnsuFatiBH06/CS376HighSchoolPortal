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

    public ManagerTeacherUI(App app) {
        this.app = app;
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("Create Teacher Account");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        add(title, BorderLayout.NORTH);

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

        add(form, BorderLayout.CENTER);
        createButton.addActionListener(e -> createTeacher());
        backButton.addActionListener(e -> app.goBack());
    }

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
