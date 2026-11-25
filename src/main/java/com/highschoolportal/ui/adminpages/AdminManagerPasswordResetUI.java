package com.highschoolportal.ui.adminpages;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.format.DateTimeFormatter;

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
import com.highschoolportal.model.PasswordResetRequest;
import com.highschoolportal.services.PasswordResetService;

public class AdminManagerPasswordResetUI extends JPanel {

    private final App app;
    private final PasswordResetService service = new PasswordResetService();

    private final DefaultTableModel model = new DefaultTableModel(new Object[]{
            "Request ID", "Credentials ID", "Role",
            "First Name", "Last Name", "Email", "Requested At"
    }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(model);

    public AdminManagerPasswordResetUI(App app) {
        this.app = app;

        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel top = new JPanel(new BorderLayout());

        JButton backButton = new JButton("Back to Homepage");
        JLabel heading = new JLabel("Manager Password Reset Requests");
        heading.setFont(heading.getFont().deriveFont(21f));

        top.add(backButton, BorderLayout.WEST);
        top.add(heading, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        backButton.addActionListener(event -> app.goBack());

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton refreshButton = new JButton("Refresh");
        JButton approveButton = new JButton("Approve Request");
        JButton rejectButton = new JButton("Reject Request");

        actions.add(refreshButton);
        actions.add(approveButton);
        actions.add(rejectButton);
        add(actions, BorderLayout.SOUTH);

        refreshButton.addActionListener(event -> refreshRequests());
        approveButton.addActionListener(event -> handleApprove());
        rejectButton.addActionListener(event -> handleReject());

        refreshRequests();
    }

    private int selectedRequestID() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow < 0) {
            return -1;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        return (Integer) model.getValueAt(modelRow, 0);
    }

    private int adminCredentialsID() {
        if (app.getCurrentUser() == null) {
            throw new IllegalStateException(
                    "A logged-in System Administrator is required.");
        }

        return app.getCurrentUser().getCredentialsID();
    }

    private void handleApprove() {
        int requestID = selectedRequestID();

        if (requestID < 0) {
            JOptionPane.showMessageDialog(this,
                    "Select a password reset request first.",
                    "Approve Request",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(this,
                "Approve this School Manager password reset request?",
                "Confirm Approval",
                JOptionPane.YES_NO_OPTION);

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            service.approveManagerRequest(requestID, adminCredentialsID());

            JOptionPane.showMessageDialog(this,
                    "The Manager password reset request was approved.");

            refreshRequests();

        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this,
                    exception.getMessage(),
                    "Approve Request",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleReject() {
        int requestID = selectedRequestID();

        if (requestID < 0) {
            JOptionPane.showMessageDialog(this,
                    "Select a password reset request first.",
                    "Reject Request",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            service.rejectManagerRequest(requestID, adminCredentialsID());

            JOptionPane.showMessageDialog(this,
                    "The Manager password reset request was rejected.");

            refreshRequests();

        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this,
                    exception.getMessage(),
                    "Reject Request",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshRequests() {
        try {
            model.setRowCount(0);

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            for (PasswordResetRequest request :
                    service.getPendingManagerRequests()) {

                model.addRow(new Object[]{
                        request.getRequestID(),
                        request.getCredentialsID(),
                        request.getRole(),
                        request.getFirstName(),
                        request.getLastName(),
                        request.getEmail(),
                        request.getRequestedAt() == null
                                ? ""
                                : request.getRequestedAt().format(formatter)
                });
            }

        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this,
                    exception.getMessage(),
                    "Load Requests",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}