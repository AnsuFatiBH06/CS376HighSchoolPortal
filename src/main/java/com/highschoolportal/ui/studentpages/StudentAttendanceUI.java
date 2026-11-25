package com.highschoolportal.ui.studentpages;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.highschoolportal.app.App;
import com.highschoolportal.model.User;
import com.highschoolportal.repository.AttendanceRepository.AttendanceEntry;
import com.highschoolportal.repository.AttendanceRepository.EnrolledSubject;
import com.highschoolportal.services.AttendanceService;
import com.highschoolportal.services.AttendanceService.AttendanceStatus;

/** Student attendance form and read-only submission history. */
public class StudentAttendanceUI extends JPanel {
    private final App app;
    private final AttendanceService service = new AttendanceService();
    private final JComboBox<EnrolledSubject> subjects = new JComboBox<>();
    private final JTextField date = new JTextField(10);
    private final JComboBox<AttendanceStatus> status = new JComboBox<>(AttendanceStatus.values());
    private final JButton submit = new JButton("Submit attendance");
    private final JButton refresh = new JButton("Refresh");
    private final JButton back = new JButton("Back");
    private final JLabel message = new JLabel(" ");
    private final DefaultTableModel history = new DefaultTableModel(
            new String[]{"Subject", "Academic year", "Date", "Week", "Attendance", "Approval"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private boolean busy;
    private long generation;

    public StudentAttendanceUI(App app) {
        this.app = app;
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(24, 28, 24, 28));
        JLabel title = new JLabel("Weekly Attendance");
        title.setFont(title.getFont().deriveFont(22f));
        JPanel header = new JPanel(new BorderLayout());
        header.add(title, BorderLayout.WEST);
        header.add(back, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        subjects.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof EnrolledSubject s) {
                    setText(s.subjectCode() + " - " + s.subjectName() + " (" + s.academicYear() + ")");
                }
                return this;
            }
        });
        form.add(new JLabel("Enrolled subject")); form.add(subjects);
        form.add(new JLabel("Session date (YYYY-MM-DD)")); form.add(date);
        form.add(new JLabel("Attendance status")); form.add(status);
        form.add(refresh); form.add(submit);
        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.add(form, BorderLayout.NORTH);
        JTable table = new JTable(history);
        table.setAutoCreateRowSorter(true);
        center.add(new JScrollPane(table), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
        add(message, BorderLayout.SOUTH);
        back.addActionListener(e -> app.goBack());
        refresh.addActionListener(e -> load());
        submit.addActionListener(e -> submitAttendance());
        subjects.addActionListener(e -> updateControls());
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (isShowing()) { date.setText(LocalDate.now().toString()); load(); }
                else { generation++; busy = false; subjects.removeAllItems(); history.setRowCount(0); }
            }
        });
        updateControls();
    }

    private void updateControls() {
        submit.setEnabled(!busy && subjects.getSelectedItem() != null);
        refresh.setEnabled(!busy);
        subjects.setEnabled(!busy);
        date.setEnabled(!busy);
        status.setEnabled(!busy);
    }

    private boolean current(User user, long ticket) {
        return generation == ticket && app.getCurrentUser() == user && isShowing();
    }

    private record Data(List<EnrolledSubject> subjects, List<AttendanceEntry> history) {}

    private void load() {
        if (busy) return;
        User user = app.getCurrentUser();
        subjects.removeAllItems(); history.setRowCount(0);
        if (user == null) { message.setText("Please log in first."); return; }
        long ticket = ++generation;
        busy = true; updateControls(); message.setText("Loading attendance...");
        new SwingWorker<Data, Void>() {
            @Override protected Data doInBackground() throws Exception {
                return new Data(service.listSubjects(user.getUserID()), service.listHistory(user.getUserID()));
            }
            @Override protected void done() {
                if (!current(user, ticket)) return;
                try {
                    Data data = get();
                    for (EnrolledSubject s : data.subjects()) subjects.addItem(s);
                    for (AttendanceEntry a : data.history()) history.addRow(new Object[]{
                            a.subjectCode() + " - " + a.subjectName(), a.academicYear(), a.sessionDate(),
                            a.weekNumber(), a.status(), a.approvalStatus()});
                    message.setText(data.subjects().isEmpty() ? "No enrolled subjects found." : "Ready.");
                } catch (Exception e) { message.setText("Unable to load attendance. Check database connection."); }
                finally { busy = false; updateControls(); }
            }
        }.execute();
    }

    private void submitAttendance() {
        User user = app.getCurrentUser();
        EnrolledSubject subject = (EnrolledSubject) subjects.getSelectedItem();
        if (busy || user == null || subject == null) return;
        LocalDate sessionDate;
        try { sessionDate = LocalDate.parse(date.getText().trim()); }
        catch (java.time.format.DateTimeParseException e) { message.setText("Use date format YYYY-MM-DD."); return; }
        AttendanceStatus selectedStatus = (AttendanceStatus) status.getSelectedItem();
        long ticket = ++generation;
        busy = true; updateControls(); message.setText("Submitting attendance...");
        new SwingWorker<Integer, Void>() {
            @Override protected Integer doInBackground() throws Exception {
                return service.recordAttendance(user.getUserID(), subject.detailID(), sessionDate, selectedStatus);
            }
            @Override protected void done() {
                if (!current(user, ticket)) return;
                busy = false; updateControls();
                try {
                    get();
                    JOptionPane.showMessageDialog(StudentAttendanceUI.this, "Attendance submitted for teacher approval.");
                    load();
                } catch (Exception e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof IllegalArgumentException) message.setText(cause.getMessage());
                    else if (cause instanceof java.sql.SQLException sql && sql.getErrorCode() == 1062)
                        message.setText("Attendance already exists for this subject and date.");
                    else message.setText("Submission could not be confirmed. Refresh before retrying.");
                }
            }
        }.execute();
    }
}
