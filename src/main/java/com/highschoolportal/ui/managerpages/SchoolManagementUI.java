package com.highschoolportal.ui.managerpages;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.highschoolportal.app.App;
import com.highschoolportal.model.Manager;
import com.highschoolportal.ui.PageNames;

public class SchoolManagementUI extends JPanel {
    private final App app;

    private final JButton backButton = new JButton("Back to Homepage");;
    private final JButton schoolRegButton = new JButton("Register School");;
    private final JButton schoolDeleteButton = new JButton("Delete School");;

    public SchoolManagementUI(App app) {
        this.app = app;
        initializeComponents();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        
        JLabel heading = new JLabel("School Management");
        heading.setFont(heading.getFont().deriveFont(22f));

        JPanel up = new JPanel(new BorderLayout());
        up.add(heading, BorderLayout.WEST);
        add(up, BorderLayout.NORTH);

        JPanel middle = new JPanel(new FlowLayout());
        middle.add(schoolRegButton);
        middle.add(schoolDeleteButton);
        add(middle, BorderLayout.CENTER);

        JPanel down = new JPanel(new BorderLayout());
        down.add(backButton, BorderLayout.WEST);
        add(down, BorderLayout.SOUTH);

        backButton.addActionListener(event -> 
            app.goBack()
        );

        schoolRegButton.addActionListener(event -> {
            Manager manager = (Manager) app.getCurrentUser();
            if (manager == null) {
                return;
            }
            if (manager.getSchoolID() >= 0) {
                JOptionPane.showMessageDialog(this, "You already have an existing school.");
                return;
            }
            app.showPage(PageNames.MANAGER_SCHOOL_SIGNUP);
        });

        schoolDeleteButton.addActionListener(event -> {

        });
    }
}
