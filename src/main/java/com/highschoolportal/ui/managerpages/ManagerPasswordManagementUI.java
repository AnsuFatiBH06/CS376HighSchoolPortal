package com.highschoolportal.ui.managerpages;

import com.highschoolportal.app.App;
import com.highschoolportal.model.PasswordResetRequest;
import com.highschoolportal.model.User;
import com.highschoolportal.services.PasswordResetService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class ManagerPasswordManagementUI extends JPanel {
    private final App app;
    private final PasswordResetService service = new PasswordResetService();
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{
            "Request ID", "Credentials ID", "Role", "First Name", "Last Name", "Email", "Requested At"
    }, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);

    public ManagerPasswordManagementUI(App app) {
        this.app = app;
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel top = new JPanel(new BorderLayout());
        JButton back = new JButton("Back to Homepage");
        JLabel heading = new JLabel("Teacher and Student Password Reset Requests");
        heading.setFont(heading.getFont().deriveFont(21f));
        top.add(back, BorderLayout.WEST);
        top.add(heading, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);
        back.addActionListener(event -> app.goBack());

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refresh = new JButton("Refresh");
        JButton reset = new JButton("Reset to DOB");
        JButton reject = new JButton("Reject Request");
        actions.add(refresh); actions.add(reset); actions.add(reject);
        add(actions, BorderLayout.SOUTH);
        refresh.addActionListener(event -> refreshRequests());
        reset.addActionListener(event -> handleReset());
        reject.addActionListener(event -> handleReject());
        refreshRequests();
    }

    private int selectedRequestID() {
        int selected = table.getSelectedRow();
        if (selected < 0) return -1;
        int modelRow = table.convertRowIndexToModel(selected);
        return (Integer) model.getValueAt(modelRow, 0);
    }

    private int managerCredentialsID() {
        User user = app.getCurrentUser();
        if (user == null) throw new IllegalStateException("A logged-in School Manager is required.");
        return user.getCredentialsID();
    }

    private void handleReset() {
        int requestID = selectedRequestID();
        if (requestID < 0) {
            JOptionPane.showMessageDialog(this, "Select a request first.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Reset this account's password to the date of birth? The user must change it after logging in.",
                "Confirm Password Reset", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            service.approveAndReset(requestID, managerCredentialsID());
            JOptionPane.showMessageDialog(this, "Password reset completed. The user must change the temporary password at next login.");
            refreshRequests();
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Password Reset", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleReject() {
        int requestID = selectedRequestID();
        if (requestID < 0) {
            JOptionPane.showMessageDialog(this, "Select a request first.");
            return;
        }
        try {
            service.reject(requestID, managerCredentialsID());
            JOptionPane.showMessageDialog(this, "Request rejected.");
            refreshRequests();
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Reject Request", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshRequests() {
        try {
            model.setRowCount(0);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (PasswordResetRequest request : service.getPendingRequests()) {
                model.addRow(new Object[]{request.getRequestID(), request.getCredentialsID(),
                        request.getRole(), request.getFirstName(), request.getLastName(),
                        request.getEmail(), request.getRequestedAt() == null ? "" : request.getRequestedAt().format(formatter)});
            }
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Load Requests", JOptionPane.ERROR_MESSAGE);
        }
    }
}
