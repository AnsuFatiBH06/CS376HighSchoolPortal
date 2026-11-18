package com.highschoolportal.ui.managerpages;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import com.highschoolportal.app.App;
import com.highschoolportal.model.Manager;
import com.highschoolportal.model.Subject;
import com.highschoolportal.services.SubjectManagementService;

public class ManagerSubjectsUI extends JPanel {

    private final App app;
    private final SubjectManagementService subjectManagementService;

    private final JTextField nameField = new JTextField(18);
    private final JTextField codeField = new JTextField(18);
    private final JTextField optionalityField = new JTextField("core", 18);
    private final JTextField passMarkField = new JTextField("60.0", 18);
    private final JTextField maxMarkField = new JTextField("100.0", 18);
    private final JTextField examSeriesNumberField = new JTextField("1", 18);

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{
                    "ID",
                    "Name",
                    "Code",
                    "Optionality",
                    "Pass Mark",
                    "Max Mark",
                    "Exam Series Number"
            }, 0) {

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable subjectsTable = new JTable(tableModel);

    private int selectedSubjectID = -1;

    public ManagerSubjectsUI(App app) {
        this(app, new SubjectManagementService());
    }

    ManagerSubjectsUI(App app, SubjectManagementService subjectManagementService) {
        this.app = app;
        this.subjectManagementService = subjectManagementService;

        initializeComponents();
        refreshSubjects();
    }

    private void initializeComponents() {

        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JLabel heading = new JLabel("Subject Management");
        heading.setFont(heading.getFont().deriveFont(22f));

        add(heading, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(12, 12));
        JPanel form = new JPanel(new GridBagLayout());

        form.setBorder(BorderFactory.createTitledBorder(
                "Create a new Subject"
        ));

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.insets = new Insets(5, 7, 5, 7);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        addField(form, constraints, 0, "Subject Name", nameField);
        addField(form, constraints, 1, "Subject Code", codeField);
        addField(form, constraints, 2, "Optionality (core/optional)", optionalityField);
        addField(form, constraints, 3, "Pass Mark (out of 100)", passMarkField);
        addField(form, constraints, 4, "Maximum Mark (out of 100)", maxMarkField);
        addField(form, constraints, 5, "Number of exams for that subject", examSeriesNumberField);

        // JLabel note = new JLabel(
        //         "Rules are saved separately for each academic year."
        // );

        JLabel note = new JLabel(
            """
            <html>
                <br />
                <p>Rules for Subject Details:</p>
                <p>1) All fields are required</p>
                <p>2) Subject Name should be between 3 and 45 characters</p>
                <p>3) Subject Name should match this regex: ^[A-Za-z0-9 '&-]+$</p>
                <p>4) Subject Code should be between 3 and 15 characters</p>
                <p>5) Subject Code can only include uppercase letters, numbers, and hyphens</p>
                <p>6) Pass and Max Marks should be positive decimal numbers</p>
                <p>7) Pass Mark should not exceed Max Mark</p>
                <p>8) Exam Series Number should be between 1 and 4</p>
            </html>
            """
        );

        constraints.gridx = 0;
        constraints.gridy = 6;
        constraints.gridwidth = 2;

        form.add(note, constraints);

        // form.add(note, constraints);

        JButton createButton = new JButton("Create Subject");
        // JButton updateButton = new JButton("Update Selected Subject's Properties");
        JButton clearButton = new JButton("Clear Selection");

        JPanel actions = new JPanel();

        actions.add(createButton);
        // actions.add(updateButton);
        actions.add(clearButton);

        JPanel formContainer = new JPanel(new BorderLayout(5, 8));

        formContainer.add(form, BorderLayout.CENTER);
        formContainer.add(actions, BorderLayout.SOUTH);

        subjectsTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        subjectsTable.setAutoCreateRowSorter(true);

        JScrollPane tableScroll = new JScrollPane(subjectsTable);

        tableScroll.setBorder(BorderFactory.createTitledBorder(
                "Your Current Subjects"
        ));

        content.add(formContainer, BorderLayout.NORTH);
        content.add(tableScroll, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());

        JButton backButton = new JButton("Back to Manager Home");

        bottom.add(backButton, BorderLayout.WEST);

        add(bottom, BorderLayout.SOUTH);

        createButton.addActionListener(event -> {
            if (showConfirmationPrompt("Are you sure of creating this subject?",
                                        "Confirm Subject Creation") == JOptionPane.YES_OPTION) {
                createSubject();
            }
        });

        // updateButton.addActionListener(event -> updateSelectedRules());
        clearButton.addActionListener(event -> {
            if (showConfirmationPrompt("Are you sure of clearing your current form", 
                                        "Clear School Details") == JOptionPane.YES_OPTION) {
                clearForm();
            }
        });
        
        backButton.addActionListener(event -> app.goBack());

        subjectsTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                loadSelectedSubject();
            }
        });
    }

    private void addField(
            JPanel panel,
            GridBagConstraints constraints,
            int row,
            String label,
            JTextField field) {

        constraints.gridwidth = 1;
        constraints.gridx = 0;
        constraints.gridy = row;

        panel.add(new JLabel(label), constraints);

        constraints.gridx = 1;

        panel.add(field, constraints);
    }

    private void createSubject() {

        Manager manager = (Manager) app.getCurrentUser();

        if (manager == null) {
            showError("There is no manager session. Please sign in as a school manager.");
            return;
        }

        try {
            float passMark = parseFloat(passMarkField, "Pass Mark");
            float maxMark = parseFloat(maxMarkField, "Max Mark");
            int examSeriesNumber = parseInt(examSeriesNumberField, "Number of Exams");

            String isItCoreStr = optionalityField.getText().toLowerCase();
            boolean isItCore;

            if (isItCoreStr.equals("core")) {
                isItCore = true;
            } else if (isItCoreStr.equals("optional")) {
                isItCore = false;
            } else {
                showError("Invalid optionality. Please type 'core' or 'optional'.");
                return;
            }

            String detailsValidError = SubjectManagementService.validateSubject(
                                                                                nameField.getText(),
                                                                                codeField.getText(),
                                                                                isItCore,
                                                                                passMark,
                                                                                maxMark,
                                                                                examSeriesNumber
                                                                            );

            if (detailsValidError == null) {
                Subject created = subjectManagementService.createSubject(
                        manager,
                        nameField.getText(),
                        codeField.getText(),
                        isItCore,
                        passMark,
                        maxMark,
                        examSeriesNumber
                );

                JOptionPane.showMessageDialog(
                        this,
                        "Subject '" + created.getSubjectName()
                                + "' was created successfully.",
                        "Subject created",
                        JOptionPane.INFORMATION_MESSAGE
                );

                clearForm();
                refreshSubjects();
            } else {
                showError(detailsValidError);
            }
        } catch (NumberFormatException exception) {

            showError(exception.getMessage());

        } catch (IllegalArgumentException | IllegalStateException exception) {

            showError(exception.getMessage());

        } catch (SQLException exception) {

            showError(databaseErrorMessage(exception));
        }
    }

    private float parseFloat(JTextField field, String fieldName) {
        try {
            return Float.parseFloat(field.getText().trim());

        } catch (NumberFormatException exception) {

            throw new NumberFormatException(
                    fieldName + " must be a decimal number (such as 60.0)."
            );
        }
    }

    private int parseInt(JTextField field, String fieldName) {
        try {
            return Integer.parseInt(field.getText().trim());

        } catch (NumberFormatException exception) {
            throw new NumberFormatException(
                fieldName + " must be a whole number (such as 2)."
            );
        }
    }

    private void refreshSubjects() {

        tableModel.setRowCount(0);

        Manager manager = (Manager) app.getCurrentUser();

        if (manager == null || manager.getSchoolID() <= 0) {
            return;
        }

        try {
            List<Subject> subjectList = subjectManagementService.getSubjects(manager);

            for (Subject subject : subjectList) {
                String isItCore = subject.getIsCore() ? "Core" : "Optional";
                tableModel.addRow(new Object[]{
                        subject.getSubjectID(),
                        subject.getSubjectName(),
                        subject.getSubjectCode(),
                        isItCore,
                        subject.getPassMark(),
                        subject.getMaxMark(),
                        subject.getExamSeriesNumber()
                });
            }

        } catch (SQLException | IllegalStateException exception) {

            showError(databaseErrorMessage(exception));
        }
    }

    private void loadSelectedSubject() {
        int viewRow = subjectsTable.getSelectedRow();

        if (viewRow < 0) {
            return;
        }

        int row = subjectsTable.convertRowIndexToModel(viewRow);

        selectedSubjectID = (int) tableModel.getValueAt(row, 0);

        nameField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        codeField.setText(String.valueOf(tableModel.getValueAt(row, 2)));
        optionalityField.setText(String.valueOf(tableModel.getValueAt(row, 3)));
        passMarkField.setText(String.valueOf(tableModel.getValueAt(row, 4)));
        maxMarkField.setText(String.valueOf(tableModel.getValueAt(row, 5)));
        examSeriesNumberField.setText(String.valueOf(tableModel.getValueAt(row, 6)));
    }

    private void clearForm() {
        selectedSubjectID = -1;
        subjectsTable.clearSelection();

        nameField.setText("");
        codeField.setText("");
        optionalityField.setText("core");

        passMarkField.setText("60.0");
        maxMarkField.setText("100.0");
        examSeriesNumberField.setText("1");
    }

    private int showConfirmationPrompt(String message, String title) {
            return JOptionPane.showConfirmDialog(
                this,
                message,
                title,
                JOptionPane.YES_NO_OPTION
        );
    }

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Subject",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private String databaseErrorMessage(Exception exception) {

        String message = exception.getMessage();

        // if (message != null
        //         && (message.contains("Unknown column")
        //         || message.contains("doesn't exist"))) {

        //     return "The database schema is missing the academic-year date columns. "
        //             + "Run the migration in "
        //             + "src/main/resources/db/migration/"
        //             + "V1_4__academic_year_dates_and_unique_name.sql "
        //             + "and try again.";
        // }

        return "The subject operation could not be completed. "
                + "Check the database connection and schema.\n"
                + (message == null
                ? exception.getClass().getSimpleName()
                : message);
    }
}