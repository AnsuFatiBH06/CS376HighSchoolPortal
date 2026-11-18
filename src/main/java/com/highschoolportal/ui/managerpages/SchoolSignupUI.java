package com.highschoolportal.ui.managerpages;
import javax.swing.*;
import java.awt.*;

import com.highschoolportal.app.App;
import com.highschoolportal.services.SchoolRegistrationService;
import com.highschoolportal.model.School;

public class SchoolSignupUI extends JPanel {
    private App app;

    private JTextField schoolNameField = new JTextField(18);
    private JTextField emailField = new JTextField(18);
    private JTextField emailDomainField = new JTextField(18);
    private JTextField addressField = new JTextField(18);;

    public SchoolSignupUI(App app) {
        this.app = app;
        initializeComponents();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JLabel heading = new JLabel("School Management");
        heading.setFont(heading.getFont().deriveFont(22f));

        add(heading, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(12, 12));
        JPanel form = new JPanel(new GridBagLayout());

        form.setBorder(BorderFactory.createTitledBorder(
                "School Details"
        ));

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.insets = new Insets(5, 7, 5, 7);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        addField(form, constraints, 0, "School Name", schoolNameField);
        addField(form, constraints, 1, "School Email (e.g. school@edu.ac.uk)", emailField);
        addField(form, constraints, 2, "Email Domain (e.g. strath.ac.uk)", emailDomainField);
        addField(form, constraints, 3, "School Address", addressField);

        constraints.gridx = 0;
        constraints.gridy = 6;
        constraints.gridwidth = 2;

        JButton registerButton = new JButton("Register School");
        JButton clearButton = new JButton("Clear Selection");

        JPanel actions = new JPanel();

        actions.add(registerButton);
        actions.add(clearButton);

        JPanel formContainer = new JPanel(new BorderLayout(5, 8));

        formContainer.add(form, BorderLayout.CENTER);
        formContainer.add(actions, BorderLayout.SOUTH);

        content.add(formContainer, BorderLayout.NORTH);

        add(content, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());

        JButton backButton = new JButton("Back to Manager Home");

        bottom.add(backButton, BorderLayout.WEST);

        add(bottom, BorderLayout.SOUTH);

        registerButton.addActionListener(event -> registerSchool());
        clearButton.addActionListener(event -> clearForm());
        backButton.addActionListener(event -> this.app.goBack());
    }

    private void addField(
            JPanel panel,
            GridBagConstraints constraints,
            int row,
            String label,
            JTextField field) {

        constraints.gridwidth = 1;
        constraints.gridx = 0;
        constraints.gridy = row;

        panel.add(new JLabel(label), constraints);

        constraints.gridx = 1;

        panel.add(field, constraints);
    }

    private void registerSchool() {
            // this.submitButton.setText("Attempting to register...");
            String detailsValidError = SchoolRegistrationService.validateSchoolDetails(
                                                                    schoolNameField.getText(), emailField.getText(),
                                                                    emailDomainField.getText(), addressField.getText());

            if (detailsValidError == null) {
                School school = new School(
                                        schoolNameField.getText(),
                                        emailField.getText(),
                                        emailDomainField.getText(),
                                        addressField.getText(),
                                        "Pending"
                                    );
                if (new SchoolRegistrationService().registerSchool(school)) {
                    JOptionPane.showMessageDialog(this, "School registration submitted.");
                    clearForm();
                    this.app.goBack();
                } else {
                    showError("School registration failed.");
                }
            } else {
                showError("School details validation failed.\nIssue: " + detailsValidError);
            }
        }

    private void clearForm() {
        schoolNameField.setText("");
        emailField.setText("");
        emailDomainField.setText("");
        addressField.setText("");
    }

    private void showError(String message) {
            JOptionPane.showMessageDialog(
                this,
                message,
                "School Management",
                JOptionPane.ERROR_MESSAGE
        );
    }
}