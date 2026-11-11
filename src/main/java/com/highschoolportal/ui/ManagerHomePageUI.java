package com.highschoolportal.ui;
import javax.swing.*;
import com.highschoolportal.app.App;


public class ManagerHomePageUI extends JPanel {
    private App app;
    private JButton schoolRegButton;
    private JButton academicYearButton;
    private JButton logoutButton;

    public ManagerHomePageUI(App app) {
        this.app = app;
        this.schoolRegButton = new JButton("School Registration");
        this.academicYearButton = new JButton("Academic Year Management");
        this.logoutButton = new JButton("Logout");

        add(this.schoolRegButton);
        add(this.academicYearButton);
        add(this.logoutButton);

        this.schoolRegButton.addActionListener(e -> {
            this.app.showPage(PageNames.SCHOOLSIGNUPPAGE);
        });

        this.logoutButton.addActionListener(e -> {
            this.app.showPage(PageNames.LOGIN);
        });
    }
}
