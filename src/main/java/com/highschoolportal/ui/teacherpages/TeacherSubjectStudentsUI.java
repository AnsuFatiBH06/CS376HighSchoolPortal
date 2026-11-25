package com.highschoolportal.ui.teacherpages;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.highschoolportal.app.App;
import com.highschoolportal.model.SubjectStudent;
import com.highschoolportal.services.TeachingService;

public class TeacherSubjectStudentsUI extends JPanel {

    private final App app;
    private final TeachingService service = new TeachingService();

    private final JComboBox<String> subjectBox = new JComboBox<>();

    private Map<Integer, String> subjectOfferings = new LinkedHashMap<>();

    private final DefaultTableModel model = new DefaultTableModel(new Object[]{
            "Student ID", "Student Name", "Email"
    }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(model);

    public TeacherSubjectStudentsUI(App app) {
        this.app = app;

        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel top = new JPanel(new BorderLayout());

        JButton backButton = new JButton("Back to Homepage");
        JLabel heading = new JLabel("Enrolled Students");
        heading.setFont(heading.getFont().deriveFont(22f));

        top.add(backButton, BorderLayout.WEST);
        top.add(heading, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        backButton.addActionListener(event -> app.goBack());

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));

        filters.add(new JLabel("Subject:"));
        filters.add(subjectBox);

        JButton viewButton = new JButton("View Students");
        filters.add(viewButton);

        add(filters, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton refreshButton = new JButton("Refresh");
        actions.add(refreshButton);

        add(actions, BorderLayout.SOUTH);

        viewButton.addActionListener(event -> viewStudents());
        refreshButton.addActionListener(event -> loadSubjects());

        loadSubjects();
    }

    private void loadSubjects() {
        try {
            int teacherID = app.getCurrentUser().getUserID();

            subjectBox.removeAllItems();

            subjectOfferings = service.getTeacherSubjectOfferings(teacherID);

            subjectBox.addItem("Select a subject");

            for (String subjectDisplay : subjectOfferings.values()) {
                subjectBox.addItem(subjectDisplay);
            }

            if (subjectOfferings.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "You are not assigned to any subjects.",
                        "Enrolled Students",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this,
                    exception.getMessage(),
                    "Load Subjects",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewStudents() {
        String selectedSubject = (String) subjectBox.getSelectedItem();

        if (selectedSubject == null
                || selectedSubject.equals("Select a subject")) {
            JOptionPane.showMessageDialog(this,
                    "Select a subject first.",
                    "Enrolled Students",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int subjectAcademicYearID = 0;

        for (Map.Entry<Integer, String> offering : subjectOfferings.entrySet()) {
            if (offering.getValue().equals(selectedSubject)) {
                subjectAcademicYearID = offering.getKey();
                break;
            }
        }

        try {
            int teacherID = app.getCurrentUser().getUserID();

            model.setRowCount(0);

            List<SubjectStudent> students = service.getEnrolledStudents(
                    teacherID,
                    subjectAcademicYearID
            );

            for (SubjectStudent student : students) {
                model.addRow(new Object[]{
                        student.getStudentID(),
                        student.getStudentName(),
                        student.getEmail()
                });
            }

            if (students.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No students are enrolled in the selected subject.",
                        "Enrolled Students",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this,
                    exception.getMessage(),
                    "Enrolled Students",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}