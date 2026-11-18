package com.highschoolportal.ui.teacherpages;

import com.highschoolportal.app.App;
import com.highschoolportal.ui.PageNames;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TeacherHomePageUI extends JPanel {
    public TeacherHomePageUI(App app) {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(24, 28, 24, 28));
        JLabel heading = new JLabel("Teacher Homepage");
        heading.setFont(heading.getFont().deriveFont(22f));
        add(heading, BorderLayout.NORTH);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton update = new JButton("Update Password");
        JButton request = new JButton("Request Password Reset");
        actions.add(update); actions.add(request);
        add(actions, BorderLayout.CENTER);
        update.addActionListener(event -> app.showPage(PageNames.UPDATE_PASSWORD));
        request.addActionListener(event -> app.showPage(PageNames.PASSWORD_RESET_REQUEST));
    }
}
