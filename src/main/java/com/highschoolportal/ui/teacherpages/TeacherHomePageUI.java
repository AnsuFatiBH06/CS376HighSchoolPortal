package com.highschoolportal.ui.teacherpages;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import com.highschoolportal.app.App;
import com.highschoolportal.ui.PageNames;

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
        JButton attendance = new JButton("Review Attendance");
        JButton marks = new JButton("Exam Marks");
        actions.add(update);
        actions.add(request);
        actions.add(attendance);
        actions.add(marks);
        add(actions, BorderLayout.CENTER);
        update.addActionListener(event -> app.showPage(PageNames.UPDATE_PASSWORD));
        request.addActionListener(event -> app.showPage(PageNames.PASSWORD_RESET_REQUEST));
        attendance.addActionListener(event -> app.showPage(PageNames.TEACHER_ATTENDANCE));
        marks.addActionListener(event -> TeacherExamMarksUI.open(app));
    }
}
