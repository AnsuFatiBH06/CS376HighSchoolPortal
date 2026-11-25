package com.highschoolportal.ui.managerpages;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.highschoolportal.app.App;
import com.highschoolportal.model.StudentSummary;
import com.highschoolportal.services.StudentManagementService;

public class ManagerAllStudentsUI extends JPanel {

    private final App app;
    private final StudentManagementService service = new StudentManagementService();

    private final DefaultTableModel model = new DefaultTableModel(new Object[]{
            "Student ID", "Student Name", "Email",
            "Academic Year", "Certificate Status"
    }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(model);

    public ManagerAllStudentsUI(App app) {
        this.app = app;

        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel top = new JPanel(new BorderLayout());

        JButton backButton = new JButton("Back to Homepage");
        JLabel heading = new JLabel("All Enrolled Students");
        heading.setFont(heading.getFont().deriveFont(22f));

        top.add(backButton, BorderLayout.WEST);
        top.add(heading, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        backButton.addActionListener(event -> app.goBack());

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton refreshButton = new JButton("Refresh");
        actions.add(refreshButton);

        add(actions, BorderLayout.SOUTH);

        refreshButton.addActionListener(event -> refreshStudents());

        refreshStudents();
    }

    private void refreshStudents() {
        try {
            int managerID = app.getCurrentUser().getUserID();

            model.setRowCount(0);

            List<StudentSummary> students = service.getAllStudents(managerID);

            for (StudentSummary student : students) {
                model.addRow(new Object[]{
                        student.getStudentID(),
                        student.getStudentName(),
                        student.getEmail(),
                        student.getAcademicYearName(),
                        student.getCertificateStatus()
                });
            }

            if (students.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No students were found in your school.",
                        "All Enrolled Students",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this,
                    exception.getMessage(),
                    "All Enrolled Students",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}