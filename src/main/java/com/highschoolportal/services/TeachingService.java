package com.highschoolportal.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.highschoolportal.database.MySQLConnect;
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
}
