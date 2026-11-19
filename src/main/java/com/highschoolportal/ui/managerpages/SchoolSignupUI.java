package com.highschoolportal.ui.managerpages;
import javax.swing.*;
import java.awt.*;

import com.highschoolportal.app.App;
import com.highschoolportal.services.SchoolRegistrationService;
import com.highschoolportal.model.Manager;
import com.highschoolportal.model.School;
import com.highschoolportal.ui.RulesDialog;

public class SchoolSignupUI extends JPanel {
    private App app;

    private JTextField schoolNameField = new JTextField(18);
    private JTextField emailField = new JTextField(18);
    private JTextField emailDomainField = new JTextField(18);
    private JTextField addressField = new JTextField(18);

    private JLabel ruleSet = new JLabel(
            """
            <html>
                <body>
                    <br />
                    <p>1) All fields are required</p>
                    <p>2) School Name be between 4 and 35 characters</p>
                    <p>3) School Name should match this regex:<br />^[A-Za-z0-9 .'-]+$</p>
                    <p>4) School Email should match this regex:<br />^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$</p>
                    <p>5) Email Domain should match this regex:<br />^[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$</p>
                    <p>6) School Email must belong to the same email domain</p>
                    <p>7) Address should be between 10 and 100 characters</p>
                    <p>8) Address should match this regex:<br />^[A-Za-z0-9 ,./#-]+$</p>
                </body>
            </html>
            """
        );

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

        // JTextArea note = new JTextArea(
        //         """
        //         Rules for School Details:
        //         1) All fields are required
        //         2) School Name should have more than 4 characters and less than 35 characters
        //         3) School Name should match this regex: ^[A-Za-z0-9 .'-]+$
        //         4) School Email should match this regex: ^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$
        //         5) Email Domain should match this regex: ^[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$
        //         6) School Email must belong to the same email domain
        //         7) Address should have between 10 and 100 characters 
        //         8) Address should match this regex: ^[A-Za-z0-9 ,./#-]+$
        //         """
        // );

        // note.setEditable(false);
        // note.setLineWrap(true);
        // note.setWrapStyleWord(true);
        // note.setOpaque(false);

        constraints.gridx = 0;
        constraints.gridy = 6;
        constraints.gridwidth = 2;

        JButton ruleSetButton = new JButton("View Registration Rules");
        form.add(ruleSetButton, constraints);

        JButton registerButton = new JButton("Register School");
        JButton clearButton = new JButton("Clear Selection");

        JPanel actions = new JPanel();

        actions.add(registerButton);
        actions.add(clearButton);

        JPanel formContainer = new JPanel(new BorderLayout(5, 8));

        formContainer.add(form, BorderLayout.NORTH);
        formContainer.add(actions, BorderLayout.SOUTH);

        content.add(formContainer, BorderLayout.NORTH);

        add(content, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());

        JButton backButton = new JButton("Back to Manager Home");

        bottom.add(backButton, BorderLayout.WEST);

        add(bottom, BorderLayout.SOUTH);

        ruleSetButton.addActionListener(event -> {
            new RulesDialog(app.getCurrentFrame(), "School Registration Rules", ruleSet.getText());
        });
        registerButton.addActionListener(event -> {
            // int option = JOptionPane.showConfirmDialog(this, "Are you sure of submitting your School Details?", "Submit School Details", JOptionPane.YES_NO_OPTION);
            if (showConfirmationPrompt("Are you sure of submitting your School Details?", 
                                        "Submit School Details") == JOptionPane.YES_OPTION) {
                registerSchool();
            }
        });
        clearButton.addActionListener(event -> {
            if (showConfirmationPrompt("Are you sure of clearing your current form", 
                                        "Clear School Details") == JOptionPane.YES_OPTION) {
                clearForm();
            }
        });
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
        Manager manager = (Manager) app.getCurrentUser();

        if (manager == null) {
            showError("There is no manager session. Please sign in as a school manager.");
            return;
        }

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

    private int showConfirmationPrompt(String message, String title) {
            return JOptionPane.showConfirmDialog(
                this,
                message,
                title,
                JOptionPane.YES_NO_OPTION
        );
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