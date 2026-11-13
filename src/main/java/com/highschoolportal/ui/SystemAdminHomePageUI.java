package com.highschoolportal.ui;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Color;

import com.highschoolportal.app.App;

public class SystemAdminHomePageUI extends JPanel {

    private final App app;

    private final JButton pendingSchoolsViewButton;
    private final JButton existingSchoolsViewButton;
    private final JButton viewPasswordRequestsButton;
    private final JButton logoutButton;

    public SystemAdminHomePageUI(App app) {
        this.app = app;

        pendingSchoolsViewButton  = new JButton("Manage Pending Schools");
        existingSchoolsViewButton = new JButton("Manage Existing Schools");
        viewPasswordRequestsButton = new JButton("Password Requests Management");
        logoutButton = new JButton("Logout");
        logoutButton.setBackground(Color.RED);

        add(pendingSchoolsViewButton);
        add(existingSchoolsViewButton);
        add(viewPasswordRequestsButton);
        add(logoutButton);

        pendingSchoolsViewButton.addActionListener(event ->
                this.app.showPage(PageNames.ADMIN_PENDING_SCHOOLS)
        );

        existingSchoolsViewButton.addActionListener(event ->
                this.app.showPage(PageNames.ADMIN_APPROVED_SCHOOLS)
        );

        viewPasswordRequestsButton.addActionListener(event ->
                this.app.showPage(PageNames.ADMIN_PASSWORD_MANAGEMENT)
        );

        logoutButton.addActionListener(event ->
                this.app.showPage(PageNames.LOGIN)
        );
    }
}