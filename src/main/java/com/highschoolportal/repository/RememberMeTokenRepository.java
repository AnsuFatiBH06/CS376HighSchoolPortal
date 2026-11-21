
package com.highschoolportal.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.OptionalInt;

import com.highschoolportal.database.MySQLConnect;

public class RememberMeTokenRepository {

    public void save(int credentialsID, String tokenHash)
            throws SQLException {

        String sql = """
            INSERT INTO RememberMeToken
                (CredentialsID, TokenHash, CreatedAt, ExpiresAt)
            VALUES
                (?, ?, UTC_TIMESTAMP(),
                 DATE_ADD(UTC_TIMESTAMP(), INTERVAL 30 DAY))
            """;

        try (Connection connection =
                     MySQLConnect.getInstance().getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, credentialsID);
            statement.setString(2, tokenHash);
            statement.executeUpdate();
        }
    }

    public OptionalInt findValidCredentialsID(String tokenHash)
            throws SQLException {

        String sql = """
            SELECT t.CredentialsID
            FROM RememberMeToken t
            JOIN Credentials c
                ON c.CredentialsID = t.CredentialsID
            WHERE t.TokenHash = ?
              AND t.ExpiresAt > UTC_TIMESTAMP()
              AND t.RevokedAt IS NULL
              AND c.Status = 'Active'
            """;

        try (Connection connection =
                     MySQLConnect.getInstance().getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, tokenHash);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return OptionalInt.of(
                        result.getInt("CredentialsID")
                    );
                }

                return OptionalInt.empty();
            }
        }
    }

    public void revoke(String tokenHash) throws SQLException {

        String sql = """
            UPDATE RememberMeToken
            SET RevokedAt = UTC_TIMESTAMP()
            WHERE TokenHash = ?
              AND RevokedAt IS NULL
            """;

        try (Connection connection =
                     MySQLConnect.getInstance().getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, tokenHash);
            statement.executeUpdate();
        }
    }
}
