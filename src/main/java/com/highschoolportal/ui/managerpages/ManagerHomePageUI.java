package com.highschoolportal.ui.managerpages;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

import com.highschoolportal.app.App;
import com.highschoolportal.ui.PageNames;

public class ManagerHomePageUI extends JPanel {

    private final App app;

    private JButton schoolRegButton;
    private JButton academicYearButton;
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
        schoolRegButton = new JButton("School Registration");
        academicYearButton = new JButton("Academic Year Management");
        logoutButton = new JButton("Logout");
        logoutButton.setBackground(Color.RED);
        pages.add(schoolRegButton);
        pages.add(academicYearButton);
        pages.add(logoutButton);

        add(pages, BorderLayout.CENTER);

        // add(schoolRegButton);
        // add(academicYearButton);
        // add(logoutButton);

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