package com.highschoolportal.ui;
import javax.swing.*;
import java.awt.*;
import com.highschoolportal.app.App;
import com.highschoolportal.services.SchoolRegistrationService;
import com.highschoolportal.model.School;

public class SchoolSignupUI extends JPanel {
    private App app;

    private JPanel formPanel;
    private JLabel formTitle;

    private JTextField schoolNameField;
    private JTextField emailField;
    private JTextField emailDomainField;
    private JTextField addressField;

    private JButton submitButton;
    private JButton backButton;

    public SchoolSignupUI(App app) {
        this.app = app;
        initializeComponents();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());
        this.formPanel = new JPanel();

        this.formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        this.formTitle = new JLabel("School Registration");
        this.formTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        this.schoolNameField = new JTextField();
        this.emailField = new JTextField();
        this.emailDomainField = new JTextField();
        this.addressField = new JTextField();

        this.schoolNameField.setMaximumSize(new Dimension(300, 30));
        this.emailField.setMaximumSize(new Dimension(300, 30));
        this.emailDomainField.setMaximumSize(new Dimension(300, 30));

        this.submitButton = new JButton("Submit");
        this.backButton = new JButton("Back");
        submitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        this.formPanel.add(this.formTitle);
        this.formPanel.add(Box.createVerticalStrut(20));

        this.formPanel.add(new JLabel("School Name"));
        this.formPanel.add(this.schoolNameField);

        this.formPanel.add(Box.createVerticalStrut(10));

        this.formPanel.add(new JLabel("Email"));
        this.formPanel.add(this.emailField);

        this.formPanel.add(Box.createVerticalStrut(10));

        this.formPanel.add(new JLabel("Email Domain"));
        this.formPanel.add(this.emailDomainField);

        this.formPanel.add(Box.createVerticalStrut(10));

        this.formPanel.add(new JLabel("Address"));
        this.formPanel.add(this.addressField);

        formPanel.add(Box.createVerticalStrut(20));

        this.formPanel.add(this.submitButton);

        this.formPanel.add(Box.createVerticalStrut(10));

        this.formPanel.add(backButton);

        JPanel wrapper = new JPanel(new GridBagLayout());

        wrapper.add(this.formPanel);

        add(wrapper, BorderLayout.CENTER);

        this.submitButton.addActionListener(e -> {
            boolean detailsValid = SchoolRegistrationService.validateSchoolDetails(
                                        this.schoolNameField,
                                        this.emailField,
                                        this.emailDomainField,
                                        this.addressField
                                    );

            if (detailsValid) {
                School school = new School(
                                        this.emailField.getText(),
                                        this.emailDomainField.getText(),
                                        this.addressField.getText(),
                                        "Pending"
                                    );
                
                if (SchoolRegistrationService.registerSchool(school)) {
                    JOptionPane.showMessageDialog(this, "School registration submitted.");
                    this.app.goBack();
                } else {
                    JOptionPane.showMessageDialog(this, "School registration failed.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "School details validation failed.");
            }
        });

        this.backButton.addActionListener(e ->
            this.app.goBack()
        );
    }
}