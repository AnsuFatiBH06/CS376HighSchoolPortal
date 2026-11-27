package com.highschoolportal.ui.studentpages;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTabbedPane;
import javax.swing.table.DefaultTableModel;

import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.DefaultListModel;

import com.highschoolportal.app.App;
import com.highschoolportal.model.Subject;
import com.highschoolportal.model.Student;
import com.highschoolportal.services.StudentCurriculumService;

public class StudentCurriculumUI extends JPanel {

    private final App app;
    private final StudentCurriculumService curriculumService;

    private JTabbedPane tabs;
    private JTable availableTable;
    private DefaultTableModel availableModel;
    private JList<String> selectedList;
    private DefaultListModel<String> selectedListModel;
    private JLabel selectionCountLabel;
    private JButton addButton;
    private JButton removeButton;
    private JButton confirmButton;
    private JButton backButton;

    private JTable coreSubjectsTable;
    private JTable selectedSubjectsTable;
    private DefaultTableModel coreModel;
    private DefaultTableModel selectedModel;

    public StudentCurriculumUI(App app) {
        this.app = app;
        this.curriculumService = new StudentCurriculumService();

        initializeComponents();
        refreshCurriculumTab();
        refreshSelectionTab();
    }

    private void initializeComponents() {
        setLayout(
                new BorderLayout(
                        10,
                        10
                )
        );

        //----------------------------------
        // TOP BAR
        //----------------------------------

        JPanel top =
                new JPanel(
                        new BorderLayout()
                );

        backButton =
                new JButton("Back to Homepage");

        confirmButton =
                new JButton(
                        "Confirm Selection"
                );

        top.add(
                backButton,
                BorderLayout.WEST
        );

        top.add(
                confirmButton,
                BorderLayout.EAST
        );

        add(
                top,
                BorderLayout.NORTH
        );

        //----------------------------------
        // TABS
        //----------------------------------

        tabs =
                new JTabbedPane();

        tabs.addTab(
                "Current Curriculum",
                createCurriculumTab()
        );

        tabs.addTab(
                "Subject Selection",
                createSelectionTab()
        );

        tabs.addChangeListener(e -> {
            int selected =
                    tabs.getSelectedIndex();

            if(selected == 0) {

                refreshCurriculumTab();

            }
            else {

                refreshSelectionTab();

            }

        });

        add(
                tabs,
                BorderLayout.CENTER
        );
    }

    private JPanel createSelectionTab() {
        JPanel panel =
                new JPanel();

        panel.setLayout(
                new BoxLayout(
                        panel,
                        BoxLayout.Y_AXIS
                )
        );

        //----------------------------------
        // Selection Counter
        //----------------------------------

        selectionCountLabel =
                new JLabel(
                        "Current Selection: 0 / 3"
                );

        panel.add(
                selectionCountLabel
        );

        panel.add(
                Box.createVerticalStrut(10)
        );

        //----------------------------------
        // Available Subjects Table
        //----------------------------------

        availableModel =
                new DefaultTableModel(

                        new Object[]{

                                "Code",

                                "Subject"

                        },

                        0

                );

        availableTable =
                new JTable(
                        availableModel
                );

        JScrollPane availableScroll =
                new JScrollPane(
                        availableTable
                );

        availableScroll.setBorder(
                BorderFactory.createTitledBorder(
                        "Available Subjects"
                )
        );

        panel.add(
                availableScroll
        );

        panel.add(
                Box.createVerticalStrut(10)
        );

        //----------------------------------
        // Add Button
        //----------------------------------

        addButton =
                new JButton(
                        "Add Subject"
                );

        addButton.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        panel.add(
                addButton
        );

        panel.add(
                Box.createVerticalStrut(10)
        );

        //----------------------------------
        // Selected Subjects List
        //----------------------------------

        selectedListModel =
                new DefaultListModel<>();

        selectedList =
                new JList<>(
                        selectedListModel
                );

        JScrollPane selectedScroll =
                new JScrollPane(
                        selectedList
                );

        selectedScroll.setBorder(
                BorderFactory.createTitledBorder(
                        "Selected Subjects"
                )
        );

        panel.add(
                selectedScroll
        );

        panel.add(
                Box.createVerticalStrut(10)
        );

        //----------------------------------
        // Remove Button
        //----------------------------------

        removeButton =
                new JButton(
                        "Remove Subject"
                );

        removeButton.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        panel.add(
                removeButton
        );

        //----------------------------------
        // Listeners
        //----------------------------------

        addButton.addActionListener(
                e -> handleAdd()
        );

        removeButton.addActionListener(
                e -> handleRemove()
        );

        return panel;
    }

    private JPanel createCurriculumTab() {

        JPanel panel =
                new JPanel();

        panel.setLayout(
                new BoxLayout(
                        panel,
                        BoxLayout.Y_AXIS
                )
        );

        //----------------------------------
        // Academic Year Label
        //----------------------------------

        JLabel yearLabel =
                new JLabel(
                        "Academic Year: "
                );

        yearLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        panel.add(yearLabel);

        panel.add(
                Box.createVerticalStrut(10)
        );

        //----------------------------------
        // Core Subjects Table
        //----------------------------------

        coreModel =
                new DefaultTableModel(

                        new Object[]{

                                "Code",

                                "Subject"

                        },

                        0

                );

        coreSubjectsTable =
                new JTable(
                        coreModel
                );

        JScrollPane coreScroll =
                new JScrollPane(
                        coreSubjectsTable
                );

        coreScroll.setBorder(

                BorderFactory
                        .createTitledBorder(
                                "Core Subjects"
                        )

        );

        panel.add(coreScroll);

        panel.add(
                Box.createVerticalStrut(15)
        );

        //----------------------------------
        // Selected Optional Subjects
        //----------------------------------

        selectedModel =
                new DefaultTableModel(

                        new Object[]{

                                "Code",

                                "Subject"

                        },

                        0

                );

        selectedSubjectsTable =
                new JTable(
                        selectedModel
                );

        JScrollPane selectedScroll =
                new JScrollPane(
                        selectedSubjectsTable
                );

        selectedScroll.setBorder(

                BorderFactory
                        .createTitledBorder(
                                "Selected Optional Subjects"
                        )

        );

        panel.add(selectedScroll);

        return panel;
    }

    private void handleAdd() {

        int row =
                availableTable
                        .getSelectedRow();

        if(row < 0)
            return;

        String code =
                (String)
                        availableModel
                                .getValueAt(
                                        row,
                                        0
                                );

        String name =
                (String)
                        availableModel
                                .getValueAt(
                                        row,
                                        1
                                );

        String subjectText =
                code
                + " - "
                + name;

        if(selectedListModel
                .contains(
                        subjectText
                ))
            return;

        selectedListModel
                .addElement(
                        subjectText
                );

        updateCounter();
    }

    private void handleRemove() {

        String selected =
                selectedList
                        .getSelectedValue();

        if(selected == null)
            return;

        selectedListModel
                .removeElement(
                        selected
                );

        updateCounter();
    }

    private void updateCounter() {

        int selectedCount =
                selectedListModel.size();

        selectionCountLabel.setText(

                "Current Selection: "

                + selectedCount

                + " / 3"

        );
    }

    private void refreshSelectionTab() {

        Student student = (Student) app.getCurrentUser();
            if (student == null) {
                JOptionPane.showMessageDialog(this, "You need to be signed in as a student first.");
                return;
            }

            int studentID = student.getUserID();

        availableModel.setRowCount(0);

        List<Subject> subjects =
                curriculumService
                        .getAvailableOptionals(
                                studentID
                        );

        for(Subject subject : subjects) {

            availableModel.addRow(

                    new Object[]{

                            subject.getSubjectCode(),

                            subject.getSubjectName()

                    }

            );
        }
    }

    private void refreshCurriculumTab() {

        Student student = (Student) app.getCurrentUser();
            if (student == null) {
                JOptionPane.showMessageDialog(this, "You need to be signed in as a student first.");
                return;
            }

            int studentID = student.getUserID();

        coreModel.setRowCount(0);

        selectedModel.setRowCount(0);

        //----------------------------------
        // Core Subjects
        //----------------------------------

        List<Subject> coreSubjects =
                curriculumService
                        .getCoreSubjects(
                                studentID
                        );

        for(Subject subject :
                coreSubjects) {

            coreModel.addRow(

                    new Object[]{

                            subject.getSubjectCode(),

                            subject.getSubjectName()

                    }

            );
        }

        //----------------------------------
        // Selected Subjects
        //----------------------------------

        List<Subject> selectedSubjects =
                curriculumService
                        .getSelectedSubjects(
                                studentID
                        );

        for(Subject subject :
                selectedSubjects) {

            selectedModel.addRow(

                    new Object[]{

                            subject.getSubjectCode(),

                            subject.getSubjectName()

                    }

            );
        }
    }
}