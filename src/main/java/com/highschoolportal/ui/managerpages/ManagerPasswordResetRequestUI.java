package com.highschoolportal.ui.managerpages;

import com.highschoolportal.services.PasswordResetService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ManagerPasswordResetRequestUI extends JPanel {

    private final PasswordResetService service = new PasswordResetService();

    private final JTextField emailField = new JTextField(25);
    private final JPasswordField newPasswordField = new JPasswordField(25);
    private final JPasswordField confirmPasswordField = new JPasswordField(25);

    public ManagerPasswordResetRequestUI() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel heading = new JLabel("Request Manager Password Reset");
        heading.setFont(heading.getFont().deriveFont(22f));
        add(heading, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.anchor = GridBagConstraints.WEST;

        addField(form, c, 0, "School email address:", emailField);
        addField(form, c, 1, "New password:", newPasswordField);
        addField(form, c, 2, "Confirm new password:", confirmPasswordField);

        JButton submitButton = new JButton("Submit Request");
        c.gridx = 1;
        c.gridy = 3;
        form.add(submitButton, c);

        add(form, BorderLayout.CENTER);

        JLabel note = new JLabel(
                "Your request will be reviewed by a System Administrator.");
        add(note, BorderLayout.SOUTH);

        submitButton.addActionListener(event -> submitRequest());
    }

    private void addField(JPanel form, GridBagConstraints c,
                          int row, String label, JComponent field) {
        c.gridx = 0;
        c.gridy = row;
        form.add(new JLabel(label), c);

        c.gridx = 1;
        form.add(field, c);
    }

    private void submitRequest() {
        String email = emailField.getText().trim();
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                    "The new password and confirmation password do not match.",
                    "Password Reset",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            service.submitManagerRequest(email, newPassword);

            emailField.setText("");
            newPasswordField.setText("");
            confirmPasswordField.setText("");

            JOptionPane.showMessageDialog(this,
                    "Your password reset request has been submitted.\n"
                            + "A System Administrator must approve it.");

        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this,
                    exception.getMessage(),
                    "Password Reset",
                    JOptionPane.WARNING_MESSAGE);
        }
    }
}