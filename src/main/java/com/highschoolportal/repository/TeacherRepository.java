package com.highschoolportal.repository;

import com.highschoolportal.database.MySQLConnect;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

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
}
