package com.highschoolportal.ui.managerpages;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.highschoolportal.app.App;
import com.highschoolportal.model.StudentEnrollmentRequest;
import com.highschoolportal.services.StudentManagementService;

public class CreateStudentUI extends JPanel {

    private final App app;
    private final StudentManagementService service = new StudentManagementService();

    private final JTextField firstNameField = new JTextField(20);
    private final JTextField lastNameField = new JTextField(20);
    private final JTextField dateOfBirthField = new JTextField(12);
    private final JTextField academicYearField = new JTextField(10);

    public CreateStudentUI(App app) {
        this.app = app;

        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel top = new JPanel(new BorderLayout());

        JButton backButton = new JButton("Back to Homepage");
        JLabel heading = new JLabel("Create Student Account");
        heading.setFont(heading.getFont().deriveFont(22f));

        top.add(backButton, BorderLayout.WEST);
        top.add(heading, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        backButton.addActionListener(event -> app.goBack());

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.anchor = GridBagConstraints.WEST;

        addField(form, c, 0, "First name:", firstNameField);
        addField(form, c, 1, "Last name:", lastNameField);
        addField(form, c, 2, "Date of birth (YYYY-MM-DD):", dateOfBirthField);
        addField(form, c, 3, "Academic year ID:", academicYearField);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton createButton = new JButton("Create Student");
        actions.add(createButton);

        add(form, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);

        createButton.addActionListener(event -> createStudent());
    }

    private void addField(JPanel form, GridBagConstraints c,
                          int row, String label, JComponent field) {
        c.gridx = 0;
        c.gridy = row;
        form.add(new JLabel(label), c);

        c.gridx = 1;
        form.add(field, c);
    }

    private void createStudent() {
        try {
            int managerID = app.getCurrentUser().getUserID();

            StudentEnrollmentRequest request = new StudentEnrollmentRequest(
                    firstNameField.getText().trim(),
                    lastNameField.getText().trim(),
                    dateOfBirthField.getText().trim(),
                    Integer.parseInt(academicYearField.getText().trim())
            );

            int studentID = service.createStudentAndEnroll(managerID, request);

            JOptionPane.showMessageDialog(this,
                    "Student account created successfully.\n"
                            + "Student ID: " + studentID);

            firstNameField.setText("");
            lastNameField.setText("");
            dateOfBirthField.setText("");
            academicYearField.setText("");

        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(this,
                    "Enter a valid academic year ID.",
                    "Create Student",
                    JOptionPane.WARNING_MESSAGE);

        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this,
                    exception.getMessage(),
                    "Create Student",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}