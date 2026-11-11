package com.highschoolportal.ui;
import javax.swing.*;
import com.highschoolportal.app.App;


public class ManagerHomePageUI extends JPanel {
    private App app;
    private JButton schoolButton;
    private JButton academicYearButton;
    private JButton logoutButton;

    public ManagerHomePageUI(App app) {
        this.app = app;
        this.schoolButton = new JButton("School Registration");
        this.academicYearButton = new JButton("Academic Year Management");

        this.logoutButton = new JButton("Logout");

        add(this.schoolButton);

        add(this.academicYearButton);

        add(this.logoutButton);

        this.logoutButton.addActionListener(e -> {

            // app.showPage("LOGIN");
        });
    }
}
