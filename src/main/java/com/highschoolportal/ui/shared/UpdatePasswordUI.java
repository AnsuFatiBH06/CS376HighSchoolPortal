package com.highschoolportal.ui.shared;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;

import com.highschoolportal.app.App;
import com.highschoolportal.model.User;
import com.highschoolportal.services.PasswordResetService;

public class UpdatePasswordUI extends JPanel {
    private final App app;
    private final PasswordResetService service = new PasswordResetService();
    private final JPasswordField currentPasswordField = new JPasswordField(20);
    private final JPasswordField newPasswordField = new JPasswordField(20);
    private final JPasswordField confirmPasswordField = new JPasswordField(20);

    public UpdatePasswordUI(App app) {
        this.app = app;
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel heading = new JLabel("Update Password");
        heading.setFont(heading.getFont().deriveFont(22f));
        add(heading, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(7, 8, 7, 8);
        c.anchor = GridBagConstraints.WEST;
        addField(form, c, 0, "Current password", currentPasswordField);
        addField(form, c, 1, "New password", newPasswordField);
        addField(form, c, 2, "Confirm new password", confirmPasswordField);
        JButton updateButton = new JButton("Update Password");
        c.gridx = 1; c.gridy = 3;
        form.add(updateButton, c);
        add(form, BorderLayout.CENTER);

        updateButton.addActionListener(event -> updatePassword());
    }

    private void addField(JPanel panel, GridBagConstraints c, int row, String label, JComponent field) {
        c.gridx = 0; c.gridy = row; panel.add(new JLabel(label + ":"), c);
        c.gridx = 1; panel.add(field, c);
    }

    private void updatePassword() {
        User user = app.getCurrentUser();
        if (user == null) {
            JOptionPane.showMessageDialog(this, "You must be logged in to update your password.");
            return;
        }
        char[] current = currentPasswordField.getPassword();
        char[] next = newPasswordField.getPassword();
        char[] confirm = confirmPasswordField.getPassword();
        try {
            service.updatePassword(user.getCredentialsID(), new String(current),
                    new String(next), new String(confirm));
            JOptionPane.showMessageDialog(this, "Your password has been updated.");
            currentPasswordField.setText("");
            newPasswordField.setText("");
            confirmPasswordField.setText("");

            app.passwordChangeCompleted();
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "Update Password", JOptionPane.WARNING_MESSAGE);
        } finally {
            Arrays.fill(current, '\0');
            Arrays.fill(next, '\0');
            Arrays.fill(confirm, '\0');
        }
    }
}
