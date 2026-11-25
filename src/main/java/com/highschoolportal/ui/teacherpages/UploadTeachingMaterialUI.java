package com.highschoolportal.ui.teacherpages;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.highschoolportal.app.App;
import com.highschoolportal.services.TeachingService;

public class UploadTeachingMaterialUI extends JPanel {

    private final App app;
    private final TeachingService service = new TeachingService();

    private final JComboBox<String> subjectBox = new JComboBox<>();

    private final JTextField weekNumberField = new JTextField(6);
    private final JTextField selectedFileField = new JTextField(25);

    private Map<Integer, String> subjectOfferings = new LinkedHashMap<>();
    private File selectedPDFFile;

    public UploadTeachingMaterialUI(App app) {
        this.app = app;

        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel top = new JPanel(new BorderLayout());

        JButton backButton = new JButton("Back to Homepage");
        JLabel heading = new JLabel("Upload Weekly Teaching Material");
        heading.setFont(heading.getFont().deriveFont(22f));

        top.add(backButton, BorderLayout.WEST);
        top.add(heading, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        backButton.addActionListener(event -> app.goBack());

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.anchor = GridBagConstraints.WEST;

        addField(form, c, 0, "Subject:", subjectBox);
        addField(form, c, 1, "Week number:", weekNumberField);
        addField(form, c, 2, "Selected PDF:", selectedFileField);

        JButton chooseFileButton = new JButton("Choose PDF");
        c.gridx = 2;
        c.gridy = 2;
        form.add(chooseFileButton, c);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton uploadButton = new JButton("Upload Material");
        actions.add(uploadButton);

        add(form, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);

        chooseFileButton.addActionListener(event -> choosePDFFile());
        uploadButton.addActionListener(event -> uploadMaterial());

        loadSubjects();
    }

    private void addField(JPanel form, GridBagConstraints c,
                          int row, String label, JComponent field) {
        c.gridx = 0;
        c.gridy = row;
        form.add(new JLabel(label), c);

        c.gridx = 1;
        form.add(field, c);
    }

    private void loadSubjects() {
        try {
            int teacherID = app.getCurrentUser().getUserID();

            subjectBox.removeAllItems();

            subjectOfferings = service.getTeacherSubjectOfferings(teacherID);

            subjectBox.addItem("Select a subject");

            for (String subjectDisplay : subjectOfferings.values()) {
                subjectBox.addItem(subjectDisplay);
            }

            if (subjectOfferings.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "You are not assigned to any subjects.",
                        "Upload Teaching Material",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this,
                    exception.getMessage(),
                    "Load Subjects",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void choosePDFFile() {
        JFileChooser fileChooser = new JFileChooser();

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            selectedPDFFile = fileChooser.getSelectedFile();
            selectedFileField.setText(selectedPDFFile.getName());
        }
    }

    private void uploadMaterial() {
        String selectedSubject = (String) subjectBox.getSelectedItem();

        if (selectedSubject == null
                || selectedSubject.equals("Select a subject")) {
            JOptionPane.showMessageDialog(this,
                    "Select a subject first.",
                    "Upload Teaching Material",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int subjectAcademicYearID = 0;

        for (Map.Entry<Integer, String> offering : subjectOfferings.entrySet()) {
            if (offering.getValue().equals(selectedSubject)) {
                subjectAcademicYearID = offering.getKey();
                break;
            }
        }

        try {
            int teacherID = app.getCurrentUser().getUserID();
            int weekNumber = Integer.parseInt(weekNumberField.getText().trim());

            String confirmation = service.uploadTeachingMaterial(
                    teacherID,
                    subjectAcademicYearID,
                    weekNumber,
                    selectedPDFFile
            );

            JOptionPane.showMessageDialog(this, confirmation);

            weekNumberField.setText("");
            selectedFileField.setText("");
            selectedPDFFile = null;

        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(this,
                    "Enter a valid week number.",
                    "Upload Teaching Material",
                    JOptionPane.WARNING_MESSAGE);

        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this,
                    exception.getMessage(),
                    "Upload Teaching Material",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}