package com.highschoolportal.ui.teacherpages;

import com.highschoolportal.app.App;
import com.highschoolportal.model.User;
import com.highschoolportal.repository.AttendanceRepository.PendingAttendance;
import com.highschoolportal.services.AttendanceService;
import com.highschoolportal.services.AttendanceService.ReviewDecision;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.util.List;

/** UR-F33: review pending attendance for the teacher's assigned subjects. */
public class TeacherAttendanceUI extends JPanel {
    private final App app;
    private final AttendanceService service = new AttendanceService();
    private final JButton approve = new JButton("Approve");
    private final JButton reject = new JButton("Reject");
    private final JButton refresh = new JButton("Refresh");
    private final JLabel message = new JLabel(" ");
    private final DefaultTableModel model = new DefaultTableModel(new String[]{
            "Student", "Subject", "Academic year", "Date", "Week", "Attendance"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
        @Override public Class<?> getColumnClass(int column) {
            return column == 4 ? Integer.class : column == 3 ? java.time.LocalDate.class : String.class;
        }
    };
    private final JTable table = new JTable(model);
    private List<PendingAttendance> entries = List.of();
    private boolean busy;
    private long generation;

    public TeacherAttendanceUI(App app) {
        this.app = app;
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(24, 28, 24, 28));
        JLabel title = new JLabel("Review Weekly Attendance");
        title.setFont(title.getFont().deriveFont(22f));
        JButton back = new JButton("Back");
        JPanel header = new JPanel(new BorderLayout());
        header.add(title, BorderLayout.WEST);
        header.add(back, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(refresh); buttons.add(approve); buttons.add(reject);
        JPanel footer = new JPanel(new BorderLayout());
        footer.add(message, BorderLayout.CENTER);
        footer.add(buttons, BorderLayout.SOUTH);
        add(footer, BorderLayout.SOUTH);
        back.addActionListener(e -> app.goBack());
        refresh.addActionListener(e -> load());
        approve.addActionListener(e -> review(ReviewDecision.Approved));
        reject.addActionListener(e -> review(ReviewDecision.Rejected));
        table.getSelectionModel().addListSelectionListener(e -> updateControls());
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (isShowing()) load();
                else { generation++; busy = false; clearRows(); updateControls(); }
            }
        });
        updateControls();
    }

    private void clearRows() {
        table.clearSelection();
        entries = List.of();
        model.setRowCount(0);
    }

    private void updateControls() {
        boolean selected = !busy && table.getSelectedRow() >= 0;
        approve.setEnabled(selected);
        reject.setEnabled(selected);
        refresh.setEnabled(!busy);
        table.setEnabled(!busy);
    }

    private boolean current(User user, long ticket) {
        return generation == ticket && app.getCurrentUser() == user && isShowing();
    }

    private void load() {
        if (busy) return;
        User user = app.getCurrentUser();
        clearRows();
        if (user == null) { message.setText("Please log in first."); updateControls(); return; }
        long ticket = ++generation;
        busy = true; updateControls(); message.setText("Loading pending attendance...");
        new SwingWorker<List<PendingAttendance>, Void>() {
            @Override protected List<PendingAttendance> doInBackground() throws Exception {
                return service.listPendingForTeacher(user.getUserID());
            }
            @Override protected void done() {
                if (!current(user, ticket)) return;
                try {
                    entries = List.copyOf(get());
                    for (PendingAttendance a : entries) model.addRow(new Object[]{
                            a.studentName(), a.subjectCode() + " - " + a.subjectName(),
                            a.academicYear(), a.sessionDate(), a.weekNumber(), a.status()});
                    message.setText(entries.isEmpty() ? "No pending attendance." : entries.size() + " pending entries.");
                } catch (Exception e) {
                    clearRows(); message.setText("Unable to load attendance. Refresh to retry.");
                } finally { busy = false; updateControls(); }
            }
        }.execute();
    }

    private void review(ReviewDecision decision) {
        User user = app.getCurrentUser();
        int row = table.getSelectedRow();
        if (busy || user == null || row < 0) return;
        PendingAttendance selected = entries.get(table.convertRowIndexToModel(row));
        int answer = JOptionPane.showConfirmDialog(this,
                (decision == ReviewDecision.Approved ? "Approve" : "Reject") + " attendance for "
                        + selected.studentName() + " in " + selected.subjectName() + " on " + selected.sessionDate() + "?",
                "Confirm review", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION || app.getCurrentUser() != user || !isShowing()) return;
        long ticket = ++generation;
        busy = true; updateControls(); message.setText("Saving review...");
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                service.reviewAttendance(user.getUserID(), selected.attendanceID(), decision);
                return null;
            }
            @Override protected void done() {
                if (!current(user, ticket)) return;
                busy = false;
                try {
                    get();
                    JOptionPane.showMessageDialog(TeacherAttendanceUI.this, "Attendance " + decision.name().toLowerCase(java.util.Locale.ROOT) + ".");
                    if (current(user, ticket)) load();
                } catch (Exception e) {
                    clearRows();
                    message.setText("Review could not be confirmed. Refresh before retrying.");
                } finally { updateControls(); }
            }
        }.execute();
    }
}
