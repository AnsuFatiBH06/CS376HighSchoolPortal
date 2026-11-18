package com.highschoolportal.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.highschoolportal.model.PasswordResetRequest;


public class PasswordResetRepository {

    // Returns the CredentialsId linked to a Manager account

    public int findCredentialsIdByManagerId(Connection connection, int managerId)
    throws SQLException {
        String sql = "SELECT CredentialsID FROM Manager WHERE ManagerID = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, managerId);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getInt("CredentialsID");
                }
                throw new SQLException("Manager account not found");
            }
        }
    }

    // Returns true if the manager has a pending password reset request
    public boolean hasPendingRequest(Connection connection, int credentialsId)
    throws SQLException {

        String sql = "SELECT 1 FROM PasswordResetRequest " + "WHERE CredentialsID = ? AND Status = 'PENDING'";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, credentialsId);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    // Inserts a pending password reset request (password must be hashed before calling this method)
    public void createRequest(Connection connection, int credentialsId, String newPasswordHash)
    throws SQLException {

        String sql = "INSERT INTO PasswordResetRequest "
                    + "(CredentialsID, NewPasswordHash, RequestedAt, Status) "
                    + "VALUES (?, ?, NOW(), 'PENDING')";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, credentialsId);
            statement.setString(2, newPasswordHash);
            statement.executeUpdate();
        }
    }

    // Returns all pending password reset requests for the system admin review page
    public List<PasswordResetRequest> findPendingRequests(Connection connection)
    throws SQLException {

        String sql = "SELECT r.RequestID, r.CredentialsID, r.NewPasswordHash, "
                    + "r.RequestedAt, r.Status, r.ReviewedByCredentialsID, r.ReviewedAt "
                    + "FROM PasswordResetRequest r "
                    + "WHERE r.Status = 'PENDING' "
                    + "ORDER BY r.RequestedAt ASC";

        List<PasswordResetRequest> requests = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                requests.add(mapRequest(result));
            }
        }
        return requests;
    }

    // Finds one pending request and locks its row
    public PasswordResetRequest findPendingRequestForUpdate(Connection connection, int requestId)
    throws SQLException {

        String sql = "SELECT r.RequestID, r.CredentialsID, r.NewPasswordHash, "
                    + "r.RequestedAt, r.Status, r.ReviewedByCredentialsID, r.ReviewedAt "
                    + "FROM PasswordResetRequest r "
                    + "WHERE r.RequestID = ? AND r.Status = 'PENDING' "
                    + "FOR UPDATE";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, requestId);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return mapRequest(result);
                }
                return null;
            }
        }

    }

    // Applies the requested password hash to the manager's credentials record
    public void applyRequestedPassword(Connection connection, int credentialsId, String newPasswordHash)
    throws SQLException {

        String sql = "UPDATE Credentials "
                    + "SET PasswordHash = ?, MustChangePassword = FALSE "
                    + "WHERE CredentialsID = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newPasswordHash);
            statement.setInt(2, credentialsId);
            statement.executeUpdate();
        }
    }

    // Marks a password reset request as approved or rejected
    public void reviewRequest(Connection connection, int requestId, String newStatus, int reviewedByCredentialsId)
    throws SQLException {

        String sql = "UPDATE PasswordResetRequest "
                    + "SET Status = ?, ReviewedByCredentialsID = ?, ReviewedAt = NOW() "
                    + "WHERE RequestID = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newStatus);
            statement.setInt(2, reviewedByCredentialsId);
            statement.setInt(3, requestId);
            statement.executeUpdate();
        }
    }

    // Converts one database row into a PasswordResetRequest object
    private PasswordResetRequest mapRequest(ResultSet result) throws SQLException {

        LocalDateTime requestedAt = result.getTimestamp("RequestedAt").toLocalDateTime();

        LocalDateTime reviewedAt = result.getTimestamp("ReviewedAt") == null
                    ? null
                    : result.getTimestamp("ReviewedAt").toLocalDateTime();

        Integer reviewedByCredentialsId = result.getObject("ReviewedByCredentialsID") == null
                    ? null
                    : result.getInt("ReviewedByCredentialsID");

        return new PasswordResetRequest(
            result.getInt("RequestID"),
            result.getInt("CredentialsID"),
            result.getString("NewPasswordHash"),
            requestedAt,
            result.getString("Status"),
            reviewedByCredentialsId,
            reviewedAt
        );
    }
}