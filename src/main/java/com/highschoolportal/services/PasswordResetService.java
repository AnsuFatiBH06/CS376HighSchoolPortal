package com.highschoolportal.services;

import com.highschoolportal.database.MySQLConnect;
import com.highschoolportal.model.PasswordResetRequest;
import com.highschoolportal.repository.PasswordResetRepository;
import at.favre.lib.crypto.bcrypt.BCrypt;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;


public class PasswordResetService {

    private final PasswordResetRepository passwordResetRepository = new PasswordResetRepository();

    // School manager requests a password reset, the requested password gets hashed before being stored in PasswordResetRequest
    public void requestPasswordReset(int managerId, String newPassword, String confirmPassword)
    throws SQLException {

        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password is required");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("New password does not match with the confirmation");
        }

        validatePasswordStrength(newPassword);

        try (Connection connection = MySQLConnect.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            
            try {
                int credentialsId = passwordResetRepository.findCredentialsIdByManagerId(connection, managerId);

                if (passwordResetRepository.hasPendingRequest(connection, credentialsId)) {
                    throw new IllegalStateException("You already have a pending password reset request");
                }

                String newPasswordHash = BCrypt.withDefaults().hashToString(12, newPassword.toCharArray());

                passwordResetRepository.createRequest(connection, credentialsId, newPasswordHash);

                connection.commit();

            } catch (Exception e) {
            connection.rollback();
            throw e;
            }

        }
    }

    // System admin can view all pending manager reset requests
    public List<PasswordResetRequest> getPendingPasswordResetRequests() throws SQLException {
        try (Connection connection = MySQLConnect.getInstance().getConnection()) {
            return passwordResetRepository.findPendingRequests(connection);
        }
    }

    // System admin approves a manager reset request & the requested password hash replaces the current one
    public void approveResetRequest(int requestId, int adminCredentialsId)
    throws SQLException {

        reviewRequest(requestId, adminCredentialsId, "APPROVED", true);
    }

    // System admin rejects a reset request & current password remains unchanged
    public void rejectResetRequest(int requestId, int adminCredentialsId)
        throws SQLException {

            reviewRequest(requestId, adminCredentialsId, "REJECTED", false);
        }

    // Logic for approving or rejecting a pending request
    private void reviewRequest(int requestId, int adminCredentialsId,
                                String newStatus, boolean applyPassword)
        throws SQLException {

            try (Connection connection = MySQLConnect.getInstance().getConnection()) {
                connection.setAutoCommit(false);

                try {
                    PasswordResetRequest request = passwordResetRepository.findPendingRequestForUpdate(connection, requestId);

                    if (request == null) {
                        throw new IllegalStateException("Reset Request not found or has been already reviewed");
                    }

                    if (applyPassword) {
                        passwordResetRepository.applyRequestedPassword(
                            connection,
                            request.getCredentialsId(),
                            request.getNewPasswordHash()
                        );
                    }

                    passwordResetRepository.reviewRequest(
                        connection,
                        requestId,
                        newStatus,
                        adminCredentialsId
                    );

                    connection.commit();

                } catch (Exception e) {
                    connection.rollback();
                    throw e;
                }
            }
        }
    

    // Strong password policy
    private void validatePasswordStrength(String password) {
        boolean isValid = password.length() >= 8
                && password.matches(".*[A-Z].*")
                && password.matches(".*[a-z].*")
                && password.matches(".*\\d.*")
                && password.matches(".*[^A-Za-z0-9].*");

        if (!isValid) {
            throw new IllegalArgumentException(
                "Password must contain at least 8 characters, one uppercase letter, "
                + "one lowercase letter, one number and one special character");
        }
    }
}
