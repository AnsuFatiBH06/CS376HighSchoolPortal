package com.highschoolportal.ui.adminpages;
import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;

import com.highschoolportal.app.App;
import com.highschoolportal.model.SystemAdmin;
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
                ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

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
                    if (showConfirmationPrompt("Are you sure of approving this school?", "Confirm School Approval") == JOptionPane.YES_OPTION) {
                        if (service.approveSchool(schoolID)) {
                            JOptionPane.showMessageDialog(this, "School has been approved.");
                            // app.goBack();
                        }
                    }
                }

                case 1 -> {
                    if (showConfirmationPrompt("Are you sure of rejecting this school?", "Confirm School Rejection") == JOptionPane.YES_OPTION) {
                        if (service.rejectSchool(schoolID)) {
                            // need to add the code for selecting justification + emailing to manager
                            String[] rejectionReasons = fillRejectionMessages();
                            if (rejectionReasons == null) {
                                return;
                            } else {
                                service.generateEmail(rejectionReasons);
                                // should be used in future releases to actually send the email
                            }
                            JOptionPane.showMessageDialog(this, "School has been rejected.");
                            // app.goBack();
                        }
                    }
                }
            }
            refreshSchools();
        } catch (Exception exception) {
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

    private int showConfirmationPrompt(String message, String title) {
            return JOptionPane.showConfirmDialog(
                this,
                message,
                title,
                JOptionPane.YES_NO_OPTION
        );
    }

    private String[] fillRejectionMessages() {
        JTextField reason1 = new JTextField(25);
        JTextField reason2 = new JTextField(25);
        JTextField reason3 = new JTextField(25);

        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(new JLabel("Provide at least one rejection reason:"));
        panel.add(Box.createVerticalStrut(10));
        panel.add(new JLabel("Reason 1"));
        panel.add(reason1);

        panel.add(Box.createVerticalStrut(5));
        panel.add(new JLabel("Reason 2"));
        panel.add(reason2);
        panel.add(Box.createVerticalStrut(5));

        panel.add(new JLabel("Reason 3"));
        panel.add(reason3);

        int result = JOptionPane.showConfirmDialog(
                        this,
                        panel,
                        "Reject School",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

        if (result == JOptionPane.OK_OPTION) {
            if ((reason1.getText().isBlank() || reason1.getText() == null) &&
                (reason2.getText().isBlank() || reason2.getText() == null) &&
                (reason3.getText().isBlank() || reason3.getText() == null)
            ) {
                JOptionPane.showMessageDialog(this, "You need to input at least 1 rejection reason.");
                return null;
            }

            String[] reasons = {reason1.getText(), reason2.getText(), reason3.getText()};

            return reasons;
        }
        return null;
    }
}