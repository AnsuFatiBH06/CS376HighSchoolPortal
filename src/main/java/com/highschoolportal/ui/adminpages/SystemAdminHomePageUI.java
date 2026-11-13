package com.highschoolportal.ui.adminpages;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

import com.highschoolportal.app.App;
import com.highschoolportal.ui.PageNames;

public class SystemAdminHomePageUI extends JPanel {

    private final App app;

    private JButton pendingSchoolsViewButton;
    private JButton existingSchoolsViewButton;
    private JButton viewPasswordRequestsButton;
    private JButton logoutButton;

    public SystemAdminHomePageUI(App app) {
        this.app = app;
        initializeComponents();
    }

    public void initializeComponents() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JPanel up = new JPanel(new BorderLayout());
        JLabel heading = new JLabel("System Admin HomePage");
        heading.setFont(heading.getFont().deriveFont(22f));
        up.add(heading, BorderLayout.CENTER);
        add(up, BorderLayout.NORTH);

        JPanel pages = new JPanel(new FlowLayout());
        pendingSchoolsViewButton  = new JButton("Pending Schools");
        existingSchoolsViewButton = new JButton("Existing Schools");
        viewPasswordRequestsButton = new JButton("Password Reset Requests");
        logoutButton = new JButton("Logout");
        logoutButton.setBackground(Color.RED);
        pages.add(pendingSchoolsViewButton);
        pages.add(existingSchoolsViewButton);
        pages.add(logoutButton);

        add(pages, BorderLayout.CENTER);

        // add(pendingSchoolsViewButton);
        // add(existingSchoolsViewButton);
        // add(viewPasswordRequestsButton);
        // add(logoutButton);

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