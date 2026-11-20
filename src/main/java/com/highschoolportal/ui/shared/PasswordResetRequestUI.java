package com.highschoolportal.ui.shared;

import com.highschoolportal.services.PasswordResetService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PasswordResetRequestUI extends JPanel {
    private final PasswordResetService service = new PasswordResetService();
    private final JTextField emailField = new JTextField(25);

    public PasswordResetRequestUI() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel heading = new JLabel("Request Password Reset");
        heading.setFont(heading.getFont().deriveFont(22f));
        add(heading, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0; c.gridy = 0;
        form.add(new JLabel("School email address:"), c);
        c.gridx = 1;
        form.add(emailField, c);

        JButton submitButton = new JButton("Submit Request");
        c.gridx = 1; c.gridy = 1;
        form.add(submitButton, c);
        add(form, BorderLayout.CENTER);

        JLabel note = new JLabel("For Teacher and Student accounts only.");
        add(note, BorderLayout.SOUTH);
        submitButton.addActionListener(event -> submitRequest());
    }

    private void submitRequest() {
        try {
            service.submitRequest(emailField.getText());
            emailField.setText("");
            JOptionPane.showMessageDialog(this,
                    "Your request has been submitted. A School Manager must review it.");
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "Password Reset", JOptionPane.WARNING_MESSAGE);
        }
    }
}
