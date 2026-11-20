package com.highschoolportal.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import com.highschoolportal.database.MySQLConnect;
import com.highschoolportal.model.Credentials;

public class CredentialsRepository {

    public Optional<Credentials> findById(int credentialsID)
            throws SQLException {

        String sql = """
        SELECT CredentialsID, PasswordHash, Role,
               Status, MustChangePassword
        FROM Credentials
        WHERE CredentialsID = ?
        """;

        try (Connection connection
                = MySQLConnect.getInstance().getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(1, credentialsID);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return Optional.empty();
                }

                return Optional.of(new Credentials(
                        result.getInt("CredentialsID"),
                        Credentials.Role.valueOf(result.getString("Role")),
                        result.getString("PasswordHash"),
                        Credentials.Status.valueOf(result.getString("Status")),
                        result.getBoolean("MustChangePassword")
                ));
            }
        }
    }

    public Optional<Credentials> findByEmail(String email)
            throws SQLException {

        String sql = """
            SELECT
                c.CredentialsID,
                c.PasswordHash,
                c.Role,
                c.Status,
                c.MustChangePassword
            FROM Credentials c
            JOIN (
                SELECT CredentialsID, Email,
                       'Admin' AS AccountRole
                FROM SystemAdmin

                UNION ALL

                SELECT CredentialsID, Email,
                       'Manager' AS AccountRole
                FROM Manager

                UNION ALL

                SELECT CredentialsID, Email,
                       'Teacher' AS AccountRole
                FROM Teacher

                UNION ALL

                SELECT CredentialsID, Email,
                       'Student' AS AccountRole
                FROM Student
            ) accounts
                ON accounts.CredentialsID = c.CredentialsID
               AND accounts.AccountRole = c.Role
            WHERE accounts.Email = ?
            """;

        try (Connection connection
                = MySQLConnect.getInstance().getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setString(1, email.trim());

            try (ResultSet result = statement.executeQuery()) {

                if (!result.next()) {
                    return Optional.empty();
                }

                Credentials credentials = new Credentials(
                        result.getInt("CredentialsID"),
                        Credentials.Role.valueOf(
                                result.getString("Role")
                        ),
                        result.getString("PasswordHash"),
                        Credentials.Status.valueOf(
                                result.getString("Status")
                        ),
                        result.getBoolean("MustChangePassword")
                );

                if (result.next()) {
                    throw new SQLException(
                            "Multiple accounts found for this email"
                    );
                }

                return Optional.of(credentials);
            }
        }
    }
}
