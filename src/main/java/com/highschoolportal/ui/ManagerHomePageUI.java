package com.highschoolportal.ui;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Color;

import com.highschoolportal.app.App;

public class ManagerHomePageUI extends JPanel {

    private final App app;

    private final JButton schoolRegButton;
    private final JButton academicYearButton;
    private final JButton logoutButton;

    public ManagerHomePageUI(App app) {

        this.app = app;

        schoolRegButton = new JButton("School Registration");
        academicYearButton = new JButton("Academic Year Management");
        logoutButton = new JButton("Logout");
        logoutButton.setBackground(Color.RED);

        add(schoolRegButton);
        add(academicYearButton);
        add(logoutButton);

        academicYearButton.addActionListener(event ->
                this.app.showPage(PageNames.ACADEMIC_YEAR_MANAGEMENT)
        );

        schoolRegButton.addActionListener(event ->
                this.app.showPage(PageNames.SCHOOLSIGNUPPAGE)
        );

        logoutButton.addActionListener(event ->
                this.app.showPage(PageNames.LOGIN)
        );
    }
}