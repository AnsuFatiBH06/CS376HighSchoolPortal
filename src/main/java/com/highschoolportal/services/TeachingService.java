package com.highschoolportal.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.highschoolportal.database.MySQLConnect;
import com.highschoolportal.model.StudentTeachingMaterial;
import com.highschoolportal.model.SubjectStudent;

public class TeachingService {

    // Returns students enrolled in a subject taught by a teacher
    public List<SubjectStudent> getEnrolledStudents(int teacherID, int subjectAcademicYearID)
    throws SQLException {

        // A valid teacher ID is required
        if (teacherID <= 0) {
            throw new IllegalArgumentException("A logged-in Teacher is required");
        }

        // A valid subject must be selected
        if (subjectAcademicYearID <= 0) {
            throw new IllegalArgumentException("Select a subject");
        }

        String sql = """
                SELECT DISTINCT
                    s.StudentID,
                    CONCAT(s.FName, ' ', s.LName) AS StudentName,
                    s.Email
                FROM SubjectAcademicYear say
                JOIN Details d
                    ON d.SubjectAcademicYearID = say.SubjectAcademicYearID
                JOIN StudentAcademicYear sayStudent
                    ON sayStudent.StudentAcademicYearID = d.StudentAcademicYearID
                JOIN Student s
                    ON s.StudentID = sayStudent.StudentID
                WHERE say.SubjectAcademicYearID = ?
                  AND say.TeacherID = ?
                ORDER BY s.StudentID ASC
                """;

        List<SubjectStudent> students = new ArrayList<>();

        try (Connection connection = MySQLConnect.getInstance().getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setInt(1, subjectAcademicYearID);
                statement.setInt(2, teacherID);

                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        students.add(new SubjectStudent(
                            result.getInt("StudentID"),
                            result.getString("StudentName"),
                            result.getString("Email")
                        ));
                    }
                }
            }

            return students;
    }

    // Validates the details for uploading a weekly teaching material
    static String validateTeachingMaterialUpload(int teacherID,
                                                 int subjectAcademicYearID,
                                                 int weekNumber,
                                                 String fileName) {
        // A logged-in teacher is required
        if (teacherID <= 0) {
            return "A logged-in Teacher is required";
        }

        // A valid subject offering must be selected
        if (subjectAcademicYearID <= 0) {
            return "Select a subject";
        }

        // The week number must be positive
        if (weekNumber <= 0) {
            return "Enter a valid week number";
        }

        // A PDF file must be selected
        if (fileName == null || fileName.isBlank()) {
            return "Select a PDF file to upload";
        }

        // The file must be a PDF
        if (!fileName.toLowerCase().endsWith(".pdf")) {
            return "The teaching material must be a PDF file";
        }

        // The upload details are valid
        return null;
    }

    // Uploading pdf teaching material for a subject
    public String uploadTeachingMaterial(int teacherID, int subjectAcademicYearID, int weekNumber, File pdfFile)
    throws SQLException, IOException {

        // Validate the upload details before saving the file
        String validationError = validateTeachingMaterialUpload(
            teacherID, 
            subjectAcademicYearID, 
            weekNumber, 
            pdfFile == null ? null : pdfFile.getName()
        );

        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }

        // Ensure the teacher is assigned to the selected subject
        ensureTeacherAssignedToSubject(teacherID, subjectAcademicYearID);

        // Create the uploads folder (if it doesn't already exist)
        Path uploadFolder = Paths.get("uploads", "teaching-materials");
        Files.createDirectories(uploadFolder);

        // Create a unique file name to prevent overwriting existing files
        String originalFileName = pdfFile.getName();
        String fileExtension = originalFileName.substring(
                originalFileName.lastIndexOf("."));

        String savedFileName = "teacher_" + teacherID
                + "_subject_" + subjectAcademicYearID
                + "_week_" + weekNumber
                + "_" + System.currentTimeMillis()
                + fileExtension;

        // Save the pdf file
        Path savedFilePath = uploadFolder.resolve(savedFileName);
        Files.copy(pdfFile.toPath(), savedFilePath, StandardCopyOption.REPLACE_EXISTING);

        // Store the file location in the database
        String sql = """
                INSERT INTO TeachingMaterial
                (TeacherID, SubjectAcademicYearID, WeekNumber, FilePath, UploadDate)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
                """;

        try (Connection connection = MySQLConnect.getInstance().getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setInt(1, teacherID);
                statement.setInt(2, subjectAcademicYearID);
                statement.setInt(3, weekNumber);
                statement.setString(4, savedFilePath.toString());
                statement.executeUpdate();
            }

            return "Teaching material uploaded successfully for week " + weekNumber + "!";
    }

    // Checks that a teacher is assigned to the selected subject
    private void ensureTeacherAssignedToSubject(int teacherID, int subjectAcademicYearID)
        throws SQLException {

            String sql = """
            SELECT 1
            FROM SubjectAcademicYear
            WHERE SubjectAcademicYearID = ?
            AND TeacherID = ?
            """;

        try (Connection connection = MySQLConnect.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, subjectAcademicYearID);
            statement.setInt(2, teacherID);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalArgumentException(
                            "You are not assigned to the selected subject");
                }
            }
        }
    }

    // Returns weekly teaching materials for a subject that a student is enrolled in
    public List<StudentTeachingMaterial> getStudentTeachingMaterials(
        int studentID, int subjectAcademicYearID) throws SQLException {

        // A valid Student ID is required
        if (studentID <= 0) {
            throw new IllegalArgumentException("A logged-in Student is required.");
        }

        // A valid subject offering must be selected
        if (subjectAcademicYearID <= 0) {
            throw new IllegalArgumentException("Select a subject.");
        }

        // Make sure the student is enrolled in the selected subject
        ensureStudentEnrolledInSubject(studentID, subjectAcademicYearID);

        String sql = """
                SELECT
                    tm.MaterialID,
                    tm.WeekNumber,
                    s.Name AS SubjectName,
                    CONCAT(t.FName, ' ', t.LName) AS TeacherName,
                    tm.FilePath,
                    tm.UploadDate
                FROM TeachingMaterial tm
                JOIN SubjectAcademicYear say
                    ON say.SubjectAcademicYearID = tm.SubjectAcademicYearID
                JOIN Subject s
                    ON s.SubjectID = say.SubjectID
                JOIN Teacher t
                    ON t.TeacherID = tm.TeacherID
                WHERE tm.SubjectAcademicYearID = ?
                ORDER BY tm.WeekNumber ASC
                """;

        List<StudentTeachingMaterial> materials = new ArrayList<>();

        try (Connection connection = MySQLConnect.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, subjectAcademicYearID);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Timestamp uploadDate = result.getTimestamp("UploadDate");

                    materials.add(new StudentTeachingMaterial(
                            result.getInt("MaterialID"),
                            result.getInt("WeekNumber"),
                            result.getString("SubjectName"),
                            result.getString("TeacherName"),
                            result.getString("FilePath"),
                            uploadDate == null ? null : uploadDate.toLocalDateTime()
                    ));
                }
            }
        }

        return materials;
    }

    // Returns the pdf file for a teaching material
    public File downloadTeachingMaterial(int studentID, int materialID)
            throws SQLException, IOException {

        // A valid Student ID is required
        if (studentID <= 0) {
            throw new IllegalArgumentException("A logged-in Student is required.");
        }

        // A valid teaching material must be selected
        if (materialID <= 0) {
            throw new IllegalArgumentException("Select a teaching material.");
        }

        String sql = """
                SELECT tm.FilePath, tm.SubjectAcademicYearID
                FROM TeachingMaterial tm
                WHERE tm.MaterialID = ?
                """;

        String filePath;
        int subjectAcademicYearID;

        try (Connection connection = MySQLConnect.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, materialID);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalArgumentException(
                            "The selected teaching material was not found.");
                }

                filePath = result.getString("FilePath");
                subjectAcademicYearID = result.getInt("SubjectAcademicYearID");
            }
        }

        // Make sure the student is enrolled in the subject
        ensureStudentEnrolledInSubject(studentID, subjectAcademicYearID);

        File pdfFile = new File(filePath);

        // Make sure the stored pdf file still exists
        if (!pdfFile.exists()) {
            throw new IOException("The teaching material file could not be found.");
        }

        return pdfFile;
    }

    // Checks that a student is enrolled in the selected subject
    private void ensureStudentEnrolledInSubject(int studentID,
                                                int subjectAcademicYearID)
            throws SQLException {

        String sql = """
                SELECT 1
                FROM Details d
                JOIN StudentAcademicYear say
                    ON say.StudentAcademicYearID = d.StudentAcademicYearID
                WHERE say.StudentID = ?
                  AND d.SubjectAcademicYearID = ?
                LIMIT 1
                """;

        try (Connection connection = MySQLConnect.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, studentID);
            statement.setInt(2, subjectAcademicYearID);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalArgumentException(
                            "You are not enrolled in the selected subject.");
                }
            }
        }
    }
}



