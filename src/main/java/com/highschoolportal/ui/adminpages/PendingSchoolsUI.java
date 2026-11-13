package com.highschoolportal.ui.adminpages;
import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;

import com.highschoolportal.app.App;
import com.highschoolportal.services.SchoolAdministrationService;

public class PendingSchoolsUI extends JPanel {
    private final App app;
    private final SchoolAdministrationService service = new SchoolAdministrationService();
    private JTable schoolsTable;
    private DefaultTableModel tableModel;

    public PendingSchoolsUI(App app) {
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
        JButton actionButton = new JButton("Actions");
        up.add(actionButton, BorderLayout.EAST);

        JLabel heading = new JLabel("Pending Schools");
        heading.setFont(heading.getFont().deriveFont(22f));
        up.add(heading, BorderLayout.SOUTH);

        add(up, BorderLayout.NORTH);

        backButton.addActionListener(event ->
            app.goBack()
        );

        actionButton.addActionListener(event -> 
                handleAction()
        );

        tableModel = new DefaultTableModel(
                        new Object[]{
                                "ID",
                                "Name",
                                "Email",
                                "Email Domain",
                                "Address"
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

    private void handleAction() {
        int row = schoolsTable.getSelectedRow();

        if(row < 0) {
            JOptionPane.showMessageDialog(this, "Select a school first.");
            return;
        }

        int schoolID = (int) tableModel.getValueAt(row, 0);

        Object[] options = {
                "Approve",
                "Reject",
                "Cancel"
        };

        int choice = JOptionPane.showOptionDialog(
                        this,
                        "Choose an action.",
                        "School Approval",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]
                );

        try {
            switch(choice) {
                case 0 -> {
                    int confirmChoice = JOptionPane.showConfirmDialog(this, "Are you sure of approving this school?");
                    if (confirmChoice == 0) {
                        if (service.approveSchool(schoolID)) {
                            JOptionPane.showMessageDialog(this, "School has been approved.");
                        }
                    }
                }

                case 1 -> {
                    int confirmChoice = JOptionPane.showConfirmDialog(this, "Are you sure of rejecting this school?");
                    if (confirmChoice == 0) {
                        if (service.rejectSchool(schoolID)) {
                            // need to add the code for selecting justification + emailing to manager
                            JOptionPane.showMessageDialog(this, "School has been rejected.");
                        }
                    }
                }
            }

            refreshSchools();
        } catch(Exception exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage());
        }
    }

    private void refreshSchools() {
        try {
            tableModel.setRowCount(0);

            for(var school : service.getPendingSchools()) {
                tableModel.addRow(new Object[]{
                                school.getSchoolID(),
                                school.getSchoolName(),
                                school.getSchoolEmail(),
                                school.getSchoolEmailDomain(),
                                school.getSchoolAddress()
                        }
                );
            }

        } catch(Exception exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getMessage()
            );
        }
    }
}