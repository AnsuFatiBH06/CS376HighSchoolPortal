package com.highschoolportal.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

import com.highschoolportal.database.MySQLConnect;

public class TeacherRepository {

    public record CreatedTeacher(int teacherID, int credentialsID, int schoolID) {}

    public CreatedTeacher createForManager(
            int managerID, String firstName, String lastName,
            String email, LocalDate dateOfBirth, String passwordHash)
            throws SQLException {

        try (Connection connection = MySQLConnect.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try {
                int schoolID = getApprovedSchoolForManager(connection, managerID);
                validateSchoolEmailDomain(connection, schoolID, email);
                ensureEmailNotAlreadyRegistered(connection, email);

                int credentialsID;
                String insertCredentials = """
                        INSERT INTO Credentials
                        (PasswordHash, Role, MustChangePassword, Status)
                        VALUES (?, 'Teacher', TRUE, 'Active')
                        """;
                try (PreparedStatement statement = connection.prepareStatement(
                        insertCredentials, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, passwordHash);
                    if (statement.executeUpdate() != 1) {
                        throw new SQLException("Could not create teacher credentials");
                    }
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Missing generated credentials ID");
                        }
                        credentialsID = keys.getInt(1);
                    }
                }

                int teacherID;
                String insertTeacher = """
                        INSERT INTO Teacher
                        (FName, LName, Email, DOB, SchoolID, CredentialsID)
                        VALUES (?, ?, ?, ?, ?, ?)
                        """;
                try (PreparedStatement statement = connection.prepareStatement(
                        insertTeacher, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, firstName);
                    statement.setString(2, lastName);
                    statement.setString(3, email);
                    statement.setDate(4, java.sql.Date.valueOf(dateOfBirth));
                    statement.setInt(5, schoolID);
                    statement.setInt(6, credentialsID);
                    if (statement.executeUpdate() != 1) {
                        throw new SQLException("Could not create teacher profile");
                    }
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Missing generated teacher ID");
                        }
                        teacherID = keys.getInt(1);
                    }
                }

                connection.commit();
                return new CreatedTeacher(teacherID, credentialsID, schoolID);
            } catch (SQLException | RuntimeException ex) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    ex.addSuppressed(rollbackError);
                }
                throw ex;
            }
        }
    }

    private int getApprovedSchoolForManager(Connection connection, int managerID)
            throws SQLException {
        String sql = """
                SELECT m.SchoolID, c.Status AS AccountStatus,
                       s.Status AS SchoolStatus
                FROM Manager m
                JOIN Credentials c ON c.CredentialsID = m.CredentialsID
                JOIN School s ON s.SchoolID = m.SchoolID
                WHERE m.ManagerID = ? AND c.Role = 'Manager'
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, managerID);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()
                        || !"Active".equals(result.getString("AccountStatus"))
                        || !"Approved".equals(result.getString("SchoolStatus"))) {
                    throw new SQLException("Manager does not belong to an approved school");
                }
                return result.getInt("SchoolID");
            }
        }
    }

    private void validateSchoolEmailDomain(Connection connection, int schoolID,
            String email) throws SQLException {
        String sql = "SELECT EmailDomain FROM School WHERE SchoolID = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, schoolID);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("School was not found");
                }
                String expected = result.getString("EmailDomain");
                String actual = email.substring(email.lastIndexOf('@') + 1);
                if (!actual.equalsIgnoreCase(expected)) {
                    throw new SQLException("Teacher email must use the school email domain");
                }
            }
        }
    }

    private void ensureEmailNotAlreadyRegistered(Connection connection, String email)
            throws SQLException {
        String sql = """
                SELECT 1 FROM SystemAdmin WHERE LOWER(Email) = LOWER(?)
                UNION ALL SELECT 1 FROM Manager WHERE LOWER(Email) = LOWER(?)
                UNION ALL SELECT 1 FROM Teacher WHERE LOWER(Email) = LOWER(?)
                UNION ALL SELECT 1 FROM Student WHERE LOWER(Email) = LOWER(?)
                LIMIT 1
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 1; i <= 4; i++) {
                statement.setString(i, email);
            }
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    throw new SQLException("Email is already registered");
                }
            }
        }
    }

    // Values displayed by the manager's teacher assignment form.
    public record TeacherOption(int teacherID, String displayName) {
        @Override public String toString() { return displayName; }
    }

    public record SubjectOfferingOption(int subjectAcademicYearID, String displayName,
                                        Integer assignedTeacherID) {
        @Override public String toString() { return displayName; }
    }

    public java.util.List<TeacherOption> listTeachersForManager(int managerID) throws SQLException {
        String sql = """
                SELECT t.TeacherID, t.FName, t.LName, t.Email
                FROM Teacher t
                JOIN Credentials tc ON tc.CredentialsID = t.CredentialsID
                JOIN Manager m ON m.SchoolID = t.SchoolID
                JOIN Credentials mc ON mc.CredentialsID = m.CredentialsID
                JOIN School sc ON sc.SchoolID = m.SchoolID
                WHERE m.ManagerID = ? AND mc.Role = 'Manager'
                  AND mc.Status = 'Active' AND sc.Status = 'Approved'
                  AND tc.Role = 'Teacher' AND tc.Status = 'Active'
                ORDER BY t.LName, t.FName, t.TeacherID
                """;
        java.util.List<TeacherOption> options = new java.util.ArrayList<>();
        try (Connection connection = MySQLConnect.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, managerID);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    options.add(new TeacherOption(result.getInt("TeacherID"),
                            result.getString("FName") + " " + result.getString("LName")
                                    + " (" + result.getString("Email") + ")"));
                }
            }
        }
        return options;
    }

    public java.util.List<SubjectOfferingOption> listSubjectOfferingsForManager(int managerID)
            throws SQLException {
        String sql = """
                SELECT say.SubjectAcademicYearID, say.TeacherID,
                       s.Code, s.Name AS SubjectName, ay.Name AS AcademicYearName
                FROM SubjectAcademicYear say
                JOIN Subject s ON s.SubjectID = say.SubjectID
                JOIN AcademicYear ay ON ay.AcademicYearID = say.AcademicYearID
                JOIN Manager m ON m.SchoolID = s.SchoolID AND m.SchoolID = ay.SchoolID
                JOIN Credentials mc ON mc.CredentialsID = m.CredentialsID
                JOIN School sc ON sc.SchoolID = m.SchoolID
                WHERE m.ManagerID = ? AND mc.Role = 'Manager'
                  AND mc.Status = 'Active' AND sc.Status = 'Approved'
                ORDER BY ay.Name, s.Name, say.SubjectAcademicYearID
                """;
        java.util.List<SubjectOfferingOption> options = new java.util.ArrayList<>();
        try (Connection connection = MySQLConnect.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, managerID);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    int teacherID = result.getInt("TeacherID");
                    Integer assigned = result.wasNull() ? null : teacherID;
                    String display = result.getString("AcademicYearName") + " | "
                            + result.getString("Code") + " - " + result.getString("SubjectName")
                            + (assigned == null ? " (unassigned)" : " (assigned to #" + assigned + ")");
                    options.add(new SubjectOfferingOption(result.getInt("SubjectAcademicYearID"),
                            display, assigned));
                }
            }
        }
        return options;
    }

    /**
     * Assign/reassign a teacher only when the manager, teacher, subject and
     * academic year belong to the same approved school.
     */
    public void assignTeacherForManager(int managerID, int teacherID,
                                        int subjectAcademicYearID) throws SQLException {
        String sql = """
                UPDATE SubjectAcademicYear say
                JOIN Subject s ON s.SubjectID = say.SubjectID
                JOIN AcademicYear ay ON ay.AcademicYearID = say.AcademicYearID
                JOIN Teacher t ON t.TeacherID = ?
                JOIN Credentials tc ON tc.CredentialsID = t.CredentialsID
                JOIN Manager m ON m.ManagerID = ?
                JOIN Credentials mc ON mc.CredentialsID = m.CredentialsID
                JOIN School sc ON sc.SchoolID = m.SchoolID
                SET say.TeacherID = t.TeacherID
                WHERE say.SubjectAcademicYearID = ?
                  AND s.SchoolID = m.SchoolID
                  AND ay.SchoolID = m.SchoolID
                  AND t.SchoolID = m.SchoolID
                  AND mc.Role = 'Manager' AND mc.Status = 'Active'
                  AND tc.Role = 'Teacher' AND tc.Status = 'Active'
                  AND sc.Status = 'Approved'
                """;
        try (Connection connection = MySQLConnect.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, teacherID);
            statement.setInt(2, managerID);
            statement.setInt(3, subjectAcademicYearID);
            int updated = statement.executeUpdate();
            if (updated == 0) {
                // MySQL reports 0 changed rows when an offering already has this teacher.
                // Distinguish an idempotent assignment from a forbidden one.
                if (!hasCurrentAssignment(connection, managerID, teacherID, subjectAcademicYearID)) {
                    throw new SQLException("Assignment rejected: check manager permissions and school membership");
                }
            }
            if (updated > 1) {
                throw new SQLException("Unexpected number of updated subject offerings: " + updated);
            }
        }
    }

    private boolean hasCurrentAssignment(Connection connection, int managerID, int teacherID,
                                         int offeringID) throws SQLException {
        String sql = """
                SELECT 1 FROM SubjectAcademicYear say
                JOIN Subject s ON s.SubjectID = say.SubjectID
                JOIN AcademicYear ay ON ay.AcademicYearID = say.AcademicYearID
                JOIN Teacher t ON t.TeacherID = say.TeacherID
                JOIN Credentials tc ON tc.CredentialsID = t.CredentialsID
                JOIN Manager m ON m.SchoolID = t.SchoolID
                JOIN Credentials mc ON mc.CredentialsID = m.CredentialsID
                JOIN School sc ON sc.SchoolID = m.SchoolID
                WHERE say.SubjectAcademicYearID = ? AND say.TeacherID = ?
                  AND m.ManagerID = ? AND s.SchoolID = m.SchoolID
                  AND ay.SchoolID = m.SchoolID AND mc.Role = 'Manager'
                  AND mc.Status = 'Active' AND tc.Role = 'Teacher'
                  AND tc.Status = 'Active' AND sc.Status = 'Approved'
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, offeringID);
            statement.setInt(2, teacherID);
            statement.setInt(3, managerID);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }
}
