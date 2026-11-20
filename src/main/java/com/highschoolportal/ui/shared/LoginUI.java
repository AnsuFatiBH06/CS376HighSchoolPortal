package com.highschoolportal.ui.shared;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import com.highschoolportal.app.App;
import com.highschoolportal.services.AuthenticationService;
import com.highschoolportal.services.RememberMeCoordinator;

public class LoginUI extends JPanel {

    private final App app;
    private final AuthenticationService authService;

    private final JTextField emailField
            = new JTextField(25);

    private final JPasswordField passwordField
            = new JPasswordField(25);

    private final JButton loginButton
            = new JButton("Login");

    private final JCheckBox rememberMeCheckBox
            = new JCheckBox("Remember me for 30 days");

    public LoginUI(App app) {
        this.app = app;
        this.authService = new AuthenticationService();

        initializeComponents();
    }

    private void initializeComponents() {
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(30, 30, 30, 30));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder(
                "High School Portal Login"
        ));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 12, 10, 12);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel heading
                = new JLabel("Welcome to HSPS", SwingConstants.CENTER);

        heading.setFont(
                heading.getFont().deriveFont(Font.BOLD, 22f)
        );

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        form.add(heading, c);

        c.gridwidth = 1;
        c.gridy = 1;
        c.gridx = 0;
        form.add(new JLabel("Email:"), c);

        c.gridx = 1;
        form.add(emailField, c);

        c.gridy = 2;
        c.gridx = 0;
        form.add(new JLabel("Password:"), c);

        c.gridx = 1;
        form.add(passwordField, c);

        c.gridy = 3;
        c.gridx = 1;
        form.add(rememberMeCheckBox, c);

        c.gridy = 4;
        c.gridx = 1;
        form.add(loginButton, c);

        add(form);

        loginButton.addActionListener(e -> attemptLogin());

        passwordField.addActionListener(e -> attemptLogin());
    }

    private void attemptLogin() {
        String email = emailField.getText().trim();
        char[] password = passwordField.getPassword();

        if (email.isBlank() || password.length == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Enter your email and password.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE
            );

            Arrays.fill(password, '\0');
            return;
        }

        loginButton.setEnabled(false);
        boolean remember = rememberMeCheckBox.isSelected();

        SwingWorker<
                Optional<AuthenticationService.LoginResult>, Void> worker = new SwingWorker<>() {

            @Override

            protected Optional<AuthenticationService.LoginResult>
                    doInBackground() throws Exception {

                try {
                    Optional<AuthenticationService.LoginResult> result
                            = authService.login(email, password);

                    if (result.isPresent()
                            && remember
                            && !result.get().mustChangePassword()) {

                        new RememberMeCoordinator().remember(
                                result.get().user().getCredentialsID()
                        );
                    }

                    return result;

                } finally {
                    Arrays.fill(password, '\0');
                }
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                passwordField.setText("");

                try {
                    Optional<AuthenticationService.LoginResult> result = get();

                    if (result.isEmpty()) {
                        JOptionPane.showMessageDialog(
                                LoginUI.this,
                                "Invalid credentials or inactive account.",
                                "Login Failed",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }

                    emailField.setText("");
                    app.completeLogin(result.get());

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            LoginUI.this,
                            "Login could not be completed. Check the database connection.",
                            "Login Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
    }

    public void clearFields() {
        emailField.setText("");
        passwordField.setText("");
    }
}
