package com.highschoolportal.ui.adminpages;

import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;

import com.highschoolportal.app.App;
import com.highschoolportal.services.SchoolAdministrationService;

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
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JPanel up = new JPanel(new BorderLayout());
        JButton backButton = new JButton("Back to Homepage");
        up.add(backButton, BorderLayout.WEST);

        JLabel heading = new JLabel("Existing Schools");
        heading.setFont(heading.getFont().deriveFont(22f));
        up.add(heading, BorderLayout.SOUTH);

        add(up, BorderLayout.NORTH);

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

        schoolsTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        schoolsTable.setAutoCreateRowSorter(true);

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
