package com.highschoolportal.ui;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Color;

import com.highschoolportal.app.App;

public class SystemAdminHomePageUI extends JPanel {

    private final App app;

    private final JButton schoolViewButton;
    private final JButton viewPasswordRequestsButton;
    private final JButton logoutButton;

    public SystemAdminHomePageUI(App app) {

        this.app = app;

        schoolViewButton = new JButton("School Management");
        viewPasswordRequestsButton = new JButton("Password Requests Management");
        logoutButton = new JButton("Logout");
        logoutButton.setBackground(Color.RED);

        add(schoolViewButton);
        add(viewPasswordRequestsButton);
        add(logoutButton);

        schoolViewButton.addActionListener(event ->
                this.app.showPage(PageNames.ADMIN_SCHOOL_MANAGEMENT)
        );

        viewPasswordRequestsButton.addActionListener(event ->
                this.app.showPage(PageNames.ADMIN_PASSWORD_MANAGEMENT)
        );

        logoutButton.addActionListener(event ->
                this.app.showPage(PageNames.LOGIN)
        );
    }
}