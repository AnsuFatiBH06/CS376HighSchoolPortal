package com.highschoolportal.ui;

import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;

import com.highschoolportal.app.App;
import com.highschoolportal.services.SchoolAdministrationService;;

public class ApprovedSchoolsUI extends JPanel {
    private final App app;
    private final SchoolAdministrationService service = new SchoolAdministrationService();
    private JTable schoolsTable;
    private DefaultTableModel tableModel;

    public ApprovedSchoolsUI(App app) {
        this.app = app;
        initializeComponents();
        refreshSchools();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());

        JButton backButton = new JButton("Back to School Management");

        add(backButton, BorderLayout.NORTH);

        backButton.addActionListener(event ->
            app.goBack()
        );

        tableModel = new DefaultTableModel(
                        new Object[]{
                                "ID",
                                "Name",
                                "Email",
                                "EmailDomain",
                                "Address",
                                "Status"
                        },
                        0
                );

        schoolsTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(schoolsTable);

        add(scrollPane, BorderLayout.CENTER);
    }

    private void refreshSchools() {
        try {
            tableModel.setRowCount(0);

            for(var school : service.getApprovedSchools()) {
                tableModel.addRow(
                        new Object[]{
                                school.getSchoolID(),
                                school.getSchoolName(),
                                school.getSchoolEmail(),
                                school.getSchoolEmailDomain(),
                                school.getSchoolAddress(),
                                school.getSchoolStatus()
                        }
                );
            }
        } catch(Exception exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage());
        }
    }
}
