package com.highschoolportal.ui.adminpages;

import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;

import com.highschoolportal.app.App;
import com.highschoolportal.services.SuperManagerService;

public class ApprovedSuperManagersUI extends JPanel {
    private final App app;
    private final SuperManagerService service = new SuperManagerService();
    private JTable managersTable;
    private DefaultTableModel tableModel;

    public ApprovedSuperManagersUI(App app) {
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

        JLabel heading = new JLabel("Approved Super Managers");
        heading.setFont(heading.getFont().deriveFont(22f));
        up.add(heading, BorderLayout.SOUTH);

        add(up, BorderLayout.NORTH);

        backButton.addActionListener(event ->
            app.goBack()
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

    private void refreshManagers() {
        try {
            tableModel.setRowCount(0);

            for(var manager : service.getApprovedSuperManagers()) {
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
}
