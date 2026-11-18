package com.highschoolportal.repository;

import com.highschoolportal.database.MySQLConnect;
import com.highschoolportal.model.PasswordResetRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PasswordResetRequestRepository {

    public Integer findTeacherOrStudentCredentialsID(String email) throws SQLException {
        String sql = """
                SELECT CredentialsID
                FROM Teacher
                WHERE LOWER(Email) = LOWER(?)
                UNION
                SELECT CredentialsID
                FROM Student
                WHERE LOWER(Email) = LOWER(?)
                """;
        try (Connection connection = MySQLConnect.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email.trim());
            statement.setString(2, email.trim());
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) return null;
                int id = result.getInt("CredentialsID");
                if (result.next()) throw new SQLException("Email belongs to more than one account.");
                return id;
            }
        }
    }

    public boolean hasPendingRequest(int credentialsID) throws SQLException {
        String sql = "SELECT 1 FROM PasswordResetRequest WHERE CredentialsID = ? AND Status = 'Pending' LIMIT 1";
        try (Connection connection = MySQLConnect.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, credentialsID);
            try (ResultSet result = statement.executeQuery()) { return result.next(); }
        }
    }

    public void insertRequest(int credentialsID) throws SQLException {
        String sql = "INSERT INTO PasswordResetRequest (CredentialsID, Status, RequestedAt) VALUES (?, 'Pending', CURRENT_TIMESTAMP)";
        try (Connection connection = MySQLConnect.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, credentialsID);
            statement.executeUpdate();
        }
    }

    public List<PasswordResetRequest> findPendingRequests() throws SQLException {
        String sql = """
                SELECT pr.RequestID, pr.CredentialsID, pr.Status, pr.RequestedAt,
                       c.Role,
                       COALESCE(t.FName, s.FName) AS FName,
                       COALESCE(t.LName, s.LName) AS LName,
                       COALESCE(t.Email, s.Email) AS Email
                FROM PasswordResetRequest pr
                JOIN Credentials c ON c.CredentialsID = pr.CredentialsID
                LEFT JOIN Teacher t ON t.CredentialsID = c.CredentialsID
                LEFT JOIN Student s ON s.CredentialsID = c.CredentialsID
                WHERE pr.Status = 'Pending'
                ORDER BY pr.RequestedAt ASC
                """;
        List<PasswordResetRequest> requests = new ArrayList<>();
        try (Connection connection = MySQLConnect.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                Timestamp requested = result.getTimestamp("RequestedAt");
                requests.add(new PasswordResetRequest(
                        result.getInt("RequestID"), result.getInt("CredentialsID"),
                        result.getString("FName"), result.getString("LName"),
                        result.getString("Email"), result.getString("Role"),
                        result.getString("Status"),
                        requested == null ? null : requested.toLocalDateTime()));
            }
        }
        return requests;
    }

    public void rejectRequest(int requestID, int managerCredentialsID) throws SQLException {
        String sql = """
                UPDATE PasswordResetRequest
                SET Status = 'Rejected', ReviewedAt = CURRENT_TIMESTAMP,
                    ReviewedByCredentialsID = ?
                WHERE RequestID = ? AND Status = 'Pending'
                """;
        try (Connection connection = MySQLConnect.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, managerCredentialsID);
            statement.setInt(2, requestID);
            if (statement.executeUpdate() != 1) throw new SQLException("The request is no longer pending.");
        }
    }

    public void resetToDateOfBirth(int requestID, int managerCredentialsID) throws SQLException {
        String requestSql = """
                SELECT pr.CredentialsID, c.Role
                FROM PasswordResetRequest pr
                JOIN Credentials c ON c.CredentialsID = pr.CredentialsID
                WHERE pr.RequestID = ? AND pr.Status = 'Pending'
                FOR UPDATE
                """;
        String dobSql = """
                SELECT DOB FROM Teacher WHERE CredentialsID = ?
                UNION ALL
                SELECT DOB FROM Student WHERE CredentialsID = ?
                """;
        String credentialSql = "UPDATE Credentials SET PasswordHash = ?, MustChangePassword = TRUE WHERE CredentialsID = ?";
        String requestUpdateSql = """
                UPDATE PasswordResetRequest
                SET Status = 'Approved', ReviewedAt = CURRENT_TIMESTAMP,
                    ReviewedByCredentialsID = ?
                WHERE RequestID = ? AND Status = 'Pending'
                """;

        try (Connection connection = MySQLConnect.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try {
                int credentialsID;
                String role;
                try (PreparedStatement statement = connection.prepareStatement(requestSql)) {
                    statement.setInt(1, requestID);
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) throw new SQLException("The request is no longer pending.");
                        credentialsID = result.getInt("CredentialsID");
                        role = result.getString("Role");
                    }
                }

                if (!"Teacher".equalsIgnoreCase(role) && !"Student".equalsIgnoreCase(role))
                    throw new SQLException("Only Teacher and Student accounts can be reset here.");

                Date dob;
                try (PreparedStatement statement = connection.prepareStatement(dobSql)) {
                    statement.setInt(1, credentialsID);
                    statement.setInt(2, credentialsID);
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) throw new SQLException("Could not find the account date of birth.");
                        dob = result.getDate("DOB");
                        if (dob == null) throw new SQLException("The account has no date of birth recorded.");
                        if (result.next()) throw new SQLException("More than one Teacher/Student account uses these credentials.");
                    }
                }

                // The agreed project assumption uses DOB as the temporary password.
                String temporaryPassword = dob.toLocalDate().toString();
                String hash = at.favre.lib.crypto.bcrypt.BCrypt.withDefaults()
                        .hashToString(12, temporaryPassword.toCharArray());
                try (PreparedStatement statement = connection.prepareStatement(credentialSql)) {
                    statement.setString(1, hash);
                    statement.setInt(2, credentialsID);
                    if (statement.executeUpdate() != 1) throw new SQLException("Could not update credentials.");
                }
                try (PreparedStatement statement = connection.prepareStatement(requestUpdateSql)) {
                    statement.setInt(1, managerCredentialsID);
                    statement.setInt(2, requestID);
                    if (statement.executeUpdate() != 1) throw new SQLException("Could not complete the reset request.");
                }
                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                if (exception instanceof SQLException sqlException) throw sqlException;
                throw new SQLException("Password reset failed.", exception);
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }
}
