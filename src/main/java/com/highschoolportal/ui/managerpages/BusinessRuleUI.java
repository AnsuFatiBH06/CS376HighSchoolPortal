package com.highschoolportal.ui.managerpages;

import com.highschoolportal.app.App;
import com.highschoolportal.model.AcademicYear;
import com.highschoolportal.model.Manager;
import com.highschoolportal.services.BusinessRuleService;

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
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class BusinessRuleUI extends JPanel {

    private final App app;
    private final BusinessRuleService businessRuleService;

    private final JTextField nameField = new JTextField(18);
    private final JTextField startDateField = new JTextField(18);
    private final JTextField endDateField = new JTextField(18);
    private final JTextField minOptionalField = new JTextField("0", 18);
    private final JTextField maxOptionalField = new JTextField("0", 18);
    private final JTextField requiredPassedField = new JTextField("1", 18);

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{
                    "ID",
                    "Name",
                    "Start date",
                    "End date",
                    "Min optional",
                    "Max optional",
                    "Required passed"
            }, 0) {

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable academicYearsTable = new JTable(tableModel);

    private int selectedAcademicYearID = -1;

    public BusinessRuleUI(App app) {
        this(app, new BusinessRuleService());
    }

    BusinessRuleUI(App app, BusinessRuleService businessRuleService) {
        this.app = app;
        this.businessRuleService = businessRuleService;

        initializeComponents();
        refreshAcademicYears();
    }

    private void initializeComponents() {

        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JLabel heading = new JLabel("Academic Year Management");
        heading.setFont(heading.getFont().deriveFont(22f));

        add(heading, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(12, 12));
        JPanel form = new JPanel(new GridBagLayout());

        form.setBorder(BorderFactory.createTitledBorder(
                "Academic year details and business rules"
        ));

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.insets = new Insets(5, 7, 5, 7);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        addField(form, constraints, 0, "Academic year name", nameField);
        addField(form, constraints, 1, "Start date (YYYY-MM-DD)", startDateField);
        addField(form, constraints, 2, "End date (YYYY-MM-DD)", endDateField);
        addField(form, constraints, 3, "Minimum optional subjects", minOptionalField);
        addField(form, constraints, 4, "Maximum optional subjects", maxOptionalField);
        addField(form, constraints, 5, "Required passed subjects", requiredPassedField);

        JLabel note = new JLabel(
                "Rules are saved separately for each academic year."
        );

        constraints.gridx = 0;
        constraints.gridy = 6;
        constraints.gridwidth = 2;

        form.add(note, constraints);

        JButton createButton = new JButton("Create Academic Year");
        JButton updateButton = new JButton("Update Selected Year's Rules");
        JButton clearButton = new JButton("Clear Selection");

        JPanel actions = new JPanel();

        actions.add(createButton);
        actions.add(updateButton);
        actions.add(clearButton);

        JPanel formContainer = new JPanel(new BorderLayout(5, 8));

        formContainer.add(form, BorderLayout.CENTER);
        formContainer.add(actions, BorderLayout.SOUTH);

        academicYearsTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        academicYearsTable.setAutoCreateRowSorter(true);

        JScrollPane tableScroll = new JScrollPane(academicYearsTable);

        tableScroll.setBorder(BorderFactory.createTitledBorder(
                "Academic years for your school"
        ));

        content.add(formContainer, BorderLayout.NORTH);
        content.add(tableScroll, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());

        JButton backButton = new JButton("Back to Manager Home");

        bottom.add(backButton, BorderLayout.WEST);

        add(bottom, BorderLayout.SOUTH);

        createButton.addActionListener(event -> createAcademicYear());
        updateButton.addActionListener(event -> updateSelectedRules());
        clearButton.addActionListener(event -> clearForm());
        backButton.addActionListener(event -> app.goBack());

        academicYearsTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                loadSelectedAcademicYear();
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

    private void createAcademicYear() {

        Manager manager = app.getCurrentManager();

        if (manager == null) {
            showError(
                    "There is no manager session. Please sign in as a school manager."
            );
            return;
        }

        try {
            LocalDate startDate =
                    LocalDate.parse(startDateField.getText().trim());

            LocalDate endDate =
                    LocalDate.parse(endDateField.getText().trim());

            int minOptional = parseInteger(
                    minOptionalField, "Minimum optional subjects"
            );

            int maxOptional = parseInteger(
                    maxOptionalField, "Maximum optional subjects"
            );

            int requiredPassed = parseInteger(
                    requiredPassedField, "Required passed subjects"
            );

            AcademicYear created = businessRuleService.createAcademicYear(
                    manager,
                    nameField.getText(),
                    startDate,
                    endDate,
                    minOptional,
                    maxOptional,
                    requiredPassed
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Academic year '" + created.getName()
                            + "' was created successfully.",
                    "Academic year created",
                    JOptionPane.INFORMATION_MESSAGE
            );

            clearForm();
            refreshAcademicYears();

        } catch (DateTimeParseException exception) {

            showError(
                    "Enter dates in YYYY-MM-DD format, for example 2026-09-01."
            );

        } catch (NumberFormatException exception) {

            showError(exception.getMessage());

        } catch (IllegalArgumentException | IllegalStateException exception) {

            showError(exception.getMessage());

        } catch (SQLException exception) {

            showError(databaseErrorMessage(exception));
        }
    }

    private void updateSelectedRules() {

        Manager manager = app.getCurrentManager();

        if (manager == null) {
            showError(
                    "There is no manager session. Please sign in as a school manager."
            );
            return;
        }

        if (selectedAcademicYearID <= 0) {
            showError("Select an academic year from the table first.");
            return;
        }

        try {
            int minOptional = parseInteger(
                    minOptionalField, "Minimum optional subjects"
            );

            int maxOptional = parseInteger(
                    maxOptionalField, "Maximum optional subjects"
            );

            int requiredPassed = parseInteger(
                    requiredPassedField, "Required passed subjects"
            );

            businessRuleService.updateRules(
                    manager,
                    selectedAcademicYearID,
                    minOptional,
                    maxOptional,
                    requiredPassed
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Academic-year rules updated successfully.",
                    "Rules updated",
                    JOptionPane.INFORMATION_MESSAGE
            );

            refreshAcademicYears();

        } catch (NumberFormatException exception) {

            showError(exception.getMessage());

        } catch (IllegalArgumentException | IllegalStateException exception) {

            showError(exception.getMessage());

        } catch (SQLException exception) {

            showError(databaseErrorMessage(exception));
        }
    }

    private int parseInteger(JTextField field, String fieldName) {

        try {
            return Integer.parseInt(field.getText().trim());

        } catch (NumberFormatException exception) {

            throw new NumberFormatException(
                    fieldName + " must be a whole number."
            );
        }
    }

    private void refreshAcademicYears() {

        tableModel.setRowCount(0);

        Manager manager = app.getCurrentManager();

        if (manager == null || manager.getSchoolID() <= 0) {
            return;
        }

        try {
            List<AcademicYear> years =
                    businessRuleService.getAcademicYears(manager);

            for (AcademicYear year : years) {
                tableModel.addRow(new Object[]{
                        year.getAcademicYearID(),
                        year.getName(),
                        year.getStartDate(),
                        year.getEndDate(),
                        year.getMinOptionalSubjects(),
                        year.getMaxOptionalSubjects(),
                        year.getRequiredPassedSubjects()
                });
            }

        } catch (SQLException | IllegalStateException exception) {

            showError(databaseErrorMessage(exception));
        }
    }

    private void loadSelectedAcademicYear() {

        int viewRow = academicYearsTable.getSelectedRow();

        if (viewRow < 0) {
            return;
        }

        int row = academicYearsTable.convertRowIndexToModel(viewRow);

        selectedAcademicYearID =
                (int) tableModel.getValueAt(row, 0);

        nameField.setText(
                String.valueOf(tableModel.getValueAt(row, 1))
        );

        startDateField.setText(
                String.valueOf(tableModel.getValueAt(row, 2))
        );

        endDateField.setText(
                String.valueOf(tableModel.getValueAt(row, 3))
        );

        minOptionalField.setText(
                String.valueOf(tableModel.getValueAt(row, 4))
        );

        maxOptionalField.setText(
                String.valueOf(tableModel.getValueAt(row, 5))
        );

        requiredPassedField.setText(
                String.valueOf(tableModel.getValueAt(row, 6))
        );
    }

    private void clearForm() {

        selectedAcademicYearID = -1;

        academicYearsTable.clearSelection();

        nameField.setText("");
        startDateField.setText("");
        endDateField.setText("");

        minOptionalField.setText("0");
        maxOptionalField.setText("0");
        requiredPassedField.setText("1");
    }

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Academic year",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private String databaseErrorMessage(Exception exception) {

        String message = exception.getMessage();

        if (message != null
                && (message.contains("Unknown column")
                || message.contains("doesn't exist"))) {

            return "The database schema is missing the academic-year date columns. "
                    + "Run the migration in "
                    + "src/main/resources/db/migration/"
                    + "V1_4__academic_year_dates_and_unique_name.sql "
                    + "and try again.";
        }

        return "The academic-year operation could not be completed. "
                + "Check the database connection and schema.\n"
                + (message == null
                ? exception.getClass().getSimpleName()
                : message);
    }
}