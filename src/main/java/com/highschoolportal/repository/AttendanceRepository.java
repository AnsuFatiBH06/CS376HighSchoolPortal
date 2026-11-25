package com.highschoolportal.repository;

import com.highschoolportal.database.MySQLConnect;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Database access for student attendance; ownership is checked in SQL, not only the UI. */
public class AttendanceRepository {

    public record EnrolledSubject(int detailID, String subjectCode,
                                  String subjectName, String academicYear) {}

    public List<EnrolledSubject> listEnrolledSubjects(int studentID) throws SQLException {
        String sql = """
            SELECT d.DetailID, subj.Code, subj.Name AS SubjectName, ay.Name AS YearName
            FROM Details d
            JOIN StudentAcademicYear enrol ON enrol.StudentAcademicYearID = d.StudentAcademicYearID
            JOIN Student student ON student.StudentID = enrol.StudentID
            JOIN Credentials credentials ON credentials.CredentialsID = student.CredentialsID
            JOIN SubjectAcademicYear offering ON offering.SubjectAcademicYearID = d.SubjectAcademicYearID
            JOIN Subject subj ON subj.SubjectID = offering.SubjectID
            JOIN AcademicYear ay ON ay.AcademicYearID = enrol.AcademicYearID
            JOIN School school ON school.SchoolID = student.SchoolID
            WHERE student.StudentID = ?
              AND credentials.Role = 'Student' AND credentials.Status = 'Active'
              AND school.Status = 'Approved'
              AND student.SchoolID = subj.SchoolID
              AND student.SchoolID = ay.SchoolID
              AND offering.AcademicYearID = enrol.AcademicYearID
            ORDER BY ay.Name DESC, subj.Code
            """;
        List<EnrolledSubject> result = new ArrayList<>();
        try (Connection connection = MySQLConnect.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentID);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    result.add(new EnrolledSubject(rows.getInt("DetailID"),
                            rows.getString("Code"), rows.getString("SubjectName"),
                            rows.getString("YearName")));
                }
            }
        }
        return result;
    }

    /** Inserts a pending record only when the student owns the enrollment detail. */
    public int insertPendingAttendance(int studentID, int detailID,
                                       LocalDate sessionDate, int weekNumber,
                                       String status) throws SQLException {
        String sql = """
            INSERT INTO Attendance (DetailID, SessionDate, WeekNumber, Status, ApprovalStatus)
            SELECT d.DetailID, ?, ?, ?, 'Pending'
            FROM Details d
            JOIN StudentAcademicYear enrol ON enrol.StudentAcademicYearID = d.StudentAcademicYearID
            JOIN Student student ON student.StudentID = enrol.StudentID
            JOIN Credentials credentials ON credentials.CredentialsID = student.CredentialsID
            JOIN SubjectAcademicYear offering ON offering.SubjectAcademicYearID = d.SubjectAcademicYearID
            JOIN Subject subj ON subj.SubjectID = offering.SubjectID
            JOIN AcademicYear ay ON ay.AcademicYearID = enrol.AcademicYearID
            JOIN School school ON school.SchoolID = student.SchoolID
            WHERE student.StudentID = ? AND d.DetailID = ?
              AND credentials.Role = 'Student' AND credentials.Status = 'Active'
              AND school.Status = 'Approved'
              AND student.SchoolID = subj.SchoolID
              AND student.SchoolID = ay.SchoolID
              AND offering.AcademicYearID = enrol.AcademicYearID
              AND d.IsFrozen = 0
            """;
        try (Connection connection = MySQLConnect.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setDate(1, Date.valueOf(sessionDate));
            statement.setInt(2, weekNumber);
            statement.setString(3, status);
            statement.setInt(4, studentID);
            statement.setInt(5, detailID);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Student is not authorized to record attendance for this subject");
            }
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Attendance ID was not returned");
                return keys.getInt(1);
            }
        }
    }
    public record AttendanceEntry(String subjectCode, String subjectName, String academicYear,
                                  LocalDate sessionDate, int weekNumber, String status,
                                  String approvalStatus) {}

    public List<AttendanceEntry> listHistory(int studentID) throws SQLException {
        String sql = """
            SELECT subj.Code, subj.Name AS SubjectName, ay.Name AS YearName,
                   a.SessionDate, a.WeekNumber, a.Status, a.ApprovalStatus
            FROM Attendance a
            JOIN Details d ON d.DetailID = a.DetailID
            JOIN StudentAcademicYear enrol ON enrol.StudentAcademicYearID = d.StudentAcademicYearID
            JOIN Student student ON student.StudentID = enrol.StudentID
            JOIN Credentials c ON c.CredentialsID = student.CredentialsID
            JOIN SubjectAcademicYear offering ON offering.SubjectAcademicYearID = d.SubjectAcademicYearID
            JOIN Subject subj ON subj.SubjectID = offering.SubjectID
            JOIN AcademicYear ay ON ay.AcademicYearID = enrol.AcademicYearID
            JOIN School school ON school.SchoolID = student.SchoolID
            WHERE student.StudentID = ? AND c.Role = 'Student' AND c.Status = 'Active'
              AND school.Status = 'Approved' AND student.SchoolID = subj.SchoolID
              AND student.SchoolID = ay.SchoolID AND offering.AcademicYearID = enrol.AcademicYearID
            ORDER BY a.SessionDate DESC, subj.Code
            """;
        List<AttendanceEntry> result = new ArrayList<>();
        try (Connection connection = MySQLConnect.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentID);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    result.add(new AttendanceEntry(rows.getString("Code"), rows.getString("SubjectName"),
                            rows.getString("YearName"), rows.getDate("SessionDate").toLocalDate(),
                            rows.getInt("WeekNumber"), rows.getString("Status"), rows.getString("ApprovalStatus")));
                }
            }
        }
        return result;
    }
}
