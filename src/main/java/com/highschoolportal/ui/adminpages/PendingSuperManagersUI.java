package com.highschoolportal.ui.adminpages;

import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;

import com.highschoolportal.app.App;
import com.highschoolportal.services.SuperManagerService;

public class PendingSuperManagersUI extends JPanel {
    private final App app;
    private final SuperManagerService service = new SuperManagerService();
    private JTable managersTable;
    private DefaultTableModel tableModel;

    public PendingSuperManagersUI(App app) {
        this.app = app;
        initializeComponents();
        refreshManagers();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JPanel up = new JPanel(new BorderLayout());
        JButton backButton = new JButton("Back to Homepage");
        up.add(backButton, BorderLayout.WEST);
        JButton actionButton = new JButton("Actions");
        up.add(actionButton, BorderLayout.EAST);

        JLabel heading = new JLabel("Pending Super Managers");
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
                                "FName",
                                "LName",
                                "Email",
                                "SchoolID",
                                "CredentialsID",
                        },
                        0
                ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        managersTable = new JTable(tableModel);

        managersTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        managersTable.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(managersTable);

        add(scrollPane, BorderLayout.CENTER);
    }

    private void handleAction() {
        int row = managersTable.getSelectedRow();

        if(row < 0) {
            JOptionPane.showMessageDialog(this, "Select a manager first.");
            return;
        }

        int managerID = (int) tableModel.getValueAt(row, 0);

        Object[] options = {
                "Approve",
                "Reject",
                "Cancel"
        };

        int choice = JOptionPane.showOptionDialog(
                        this,
                        "Choose an action.",
                        "Super Manager Approval",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]
                );

        try {
            switch(choice) {
                case 0 -> {
                    if (showConfirmationPrompt("Are you sure of promoting to super manager?", "Confirm Super Manager Approval") == JOptionPane.YES_OPTION) {
                        if (service.approveSuperManager(managerID)) {
                            JOptionPane.showMessageDialog(this, "Manager has been promoted.");
                            // app.goBack();
                        }
                    }
                }

                case 1 -> {
                    if (showConfirmationPrompt("Are you sure of demoting super status to 'none'?", "Confirm Super Manager Rejection") == JOptionPane.YES_OPTION) {
                        if (service.rejectSuperManager(managerID)) {
                            JOptionPane.showMessageDialog(this, "Manager has been demoted.");
                            // app.goBack();
                        }
                    }
                }
            }
            refreshManagers();
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage());
        }
    }

    private void refreshManagers() {
        try {
            tableModel.setRowCount(0);

            for(var manager : service.getPendingSuperManagers()) {
                tableModel.addRow(new Object[]{
                                manager.getUserID(),
                                manager.getFirstName(),
                                manager.getLastName(),
                                manager.getEmail(),
                                manager.getSchoolID(),
                                manager.getCredentialsID()
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

    private int showConfirmationPrompt(String message, String title) {
            return JOptionPane.showConfirmDialog(
                this,
                message,
                title,
                JOptionPane.YES_NO_OPTION
        );
    }
}
