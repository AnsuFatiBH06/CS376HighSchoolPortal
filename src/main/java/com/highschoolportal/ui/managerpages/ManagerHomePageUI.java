package com.highschoolportal.ui.managerpages;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.highschoolportal.app.App;
import com.highschoolportal.ui.PageNames;

public class ManagerHomePageUI extends JPanel {

    private final App app;

    private JButton schoolRegButton;
    private JButton academicYearButton;
    private JButton subjectManagementButton;
    private JButton passwordRequestsButton;
    private JButton logoutButton;

    public ManagerHomePageUI(App app) {
        this.app = app;
        initializeComponents();
    }

    public void initializeComponents() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JPanel up = new JPanel(new BorderLayout());
        JLabel heading = new JLabel("School Manager HomePage");
        heading.setFont(heading.getFont().deriveFont(22f));
        up.add(heading, BorderLayout.CENTER);
        add(up, BorderLayout.NORTH);

        JPanel pages = new JPanel(new FlowLayout());
        schoolRegButton = new JButton("School Management");
        academicYearButton = new JButton("Academic Year Management");
        subjectManagementButton = new JButton("Subject Management");
        passwordRequestsButton = new JButton("Password Reset Requests");
        logoutButton = new JButton("Logout");
        logoutButton.setBackground(Color.RED);
        pages.add(schoolRegButton);
        pages.add(academicYearButton);
        pages.add(subjectManagementButton);
        pages.add(passwordRequestsButton);
        pages.add(logoutButton);

        add(pages, BorderLayout.CENTER);

        // add(schoolRegButton);
        // add(academicYearButton);
        // add(logoutButton);
        schoolRegButton.addActionListener(event
                -> this.app.showPage(PageNames.MANAGER_SCHOOL_SIGNUP)
        );

        academicYearButton.addActionListener(event
                -> this.app.showPage(PageNames.MANAGER_ACADEMIC_YEAR_MANAGEMENT)
        );

        subjectManagementButton.addActionListener(event
                -> this.app.showPage(PageNames.MANAGER_SUBJECT_MANAGEMENT)
        );

        passwordRequestsButton.addActionListener(event
                -> this.app.showPage(PageNames.MANAGER_PASSWORD_MANAGEMENT)
        );

        logoutButton.addActionListener(event -> app.logout());

    }
}
