package com.highschoolportal.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.highschoolportal.database.MySQLConnect;
import com.highschoolportal.model.PasswordResetRequest;
import com.highschoolportal.repository.PasswordResetRequestRepository;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PasswordResetService {
    private final PasswordResetRequestRepository repository = new PasswordResetRequestRepository();

    public void submitRequest(String email) throws SQLException {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Enter your school email address.");
        Integer credentialsID = repository.findTeacherOrStudentCredentialsID(email);
        if (credentialsID == null) throw new IllegalArgumentException("No Teacher or Student account was found for that email.");
        if (repository.hasPendingRequest(credentialsID)) throw new IllegalArgumentException("A password reset request is already pending.");
        repository.insertRequest(credentialsID);
    }

    public List<PasswordResetRequest> getPendingRequests() throws SQLException {
        return repository.findPendingRequests();
    }

    public void approveAndReset(int requestID, int managerCredentialsID) throws SQLException {
        ensureManager(managerCredentialsID);
        repository.resetToDateOfBirth(requestID, managerCredentialsID);
    }

    public void reject(int requestID, int managerCredentialsID) throws SQLException {
        ensureManager(managerCredentialsID);
        repository.rejectRequest(requestID, managerCredentialsID);
    }

    private void ensureManager(int credentialsID) throws SQLException {
        if (credentialsID <= 0) throw new IllegalArgumentException("A logged-in School Manager is required.");
        String sql = "SELECT Role FROM Credentials WHERE CredentialsID = ?";
        try (var connection = MySQLConnect.getInstance().getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setInt(1, credentialsID);
            try (var result = statement.executeQuery()) {
                if (!result.next() || !"Manager".equalsIgnoreCase(result.getString("Role")))
                    throw new IllegalArgumentException("Only a School Manager can process password reset requests.");
            }
        }
    }

    public void updatePassword(int credentialsID, String currentPassword, String newPassword,
                               String confirmPassword) throws SQLException {
        if (credentialsID <= 0) throw new IllegalArgumentException("A logged-in user is required.");
        if (currentPassword == null || currentPassword.isEmpty()) throw new IllegalArgumentException("Enter your current password.");
        validatePassword(newPassword);
        if (!newPassword.equals(confirmPassword)) throw new IllegalArgumentException("The new passwords do not match.");

        String selectSql = "SELECT PasswordHash FROM Credentials WHERE CredentialsID = ?";
        String updateSql = "UPDATE Credentials SET PasswordHash = ?, MustChangePassword = FALSE WHERE CredentialsID = ?";
        try (Connection connection = MySQLConnect.getInstance().getConnection()) {
            String currentHash;
            try (PreparedStatement statement = connection.prepareStatement(selectSql)) {
                statement.setInt(1, credentialsID);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) throw new IllegalArgumentException("Account credentials were not found.");
                    currentHash = result.getString("PasswordHash");
                }
            }
            BCrypt.Result verification = BCrypt.verifyer().verify(currentPassword.toCharArray(), currentHash);
            if (!verification.verified) throw new IllegalArgumentException("Your current password is incorrect.");
            String newHash = BCrypt.withDefaults().hashToString(12, newPassword.toCharArray());
            try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
                statement.setString(1, newHash);
                statement.setInt(2, credentialsID);
                if (statement.executeUpdate() != 1) throw new SQLException("Password could not be updated.");
            }
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8
                || !password.matches(".*[A-Z].*")
                || !password.matches(".*[a-z].*")
                || !password.matches(".*\\d.*")
                || !password.matches(".*[^A-Za-z0-9].*")) {
            throw new IllegalArgumentException("Password must contain at least 8 characters, an uppercase letter, a lowercase letter, a number, and a special character.");
        }
    }

    public void submitManagerRequest(String email, String newPassword)
        throws SQLException {

    if (email == null || email.isBlank()) {
        throw new IllegalArgumentException("Enter your school email address.");
    }

    if (newPassword == null || newPassword.isBlank()) {
        throw new IllegalArgumentException("Enter a new password.");
    }

    validatePassword(newPassword);

    throw new UnsupportedOperationException(
            "Manager password reset requests are not implemented yet.");
}

public java.util.List<PasswordResetRequest> getPendingManagerRequests()
        throws SQLException {

    // This will be implemented after all UI pages are added.
    throw new UnsupportedOperationException(
            "Manager password reset requests are not implemented yet.");
}

public void approveManagerRequest(int requestID, int adminCredentialsID)
        throws SQLException {

    // This will be implemented after all UI pages are added.
    throw new UnsupportedOperationException(
            "Manager password reset approval is not implemented yet.");
}

public void rejectManagerRequest(int requestID, int adminCredentialsID)
        throws SQLException {

    // This will be implemented after all UI pages are added.
    throw new UnsupportedOperationException(
            "Manager password reset rejection is not implemented yet.");
}

}
