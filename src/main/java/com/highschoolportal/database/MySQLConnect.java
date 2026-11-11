package com.highschoolportal.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnect {

    // JDBC URL (jdbc:mysql://host:3306/database)
    private final String databaseLink;

    // SQL Account Credentials
    private final String loginUsername;
    private final String loginPassword;

    // Reads from the environment variables and falls back to the defaults set by us
    private MySQLConnect() {
        String host = getEnv("DB_HOST", "devweb2026.cis.strath.ac.uk");
        String port = getEnv("DB_PORT", "3306");
        String name = getEnv("DB_NAME", "DATABASE_NAME"); // REPLACE WITH ACTUAL CREDENTIALS

        // Build the JDBC URL from the parts
        this.databaseLink = "jdbc:mysql://" + host + ":" + port + "/" + name;
        this.loginUsername = getEnv("DB_USER", "YOUR_USERNAME"); // REPLACE WITH ACTUAL CREDENTIALS
        this.loginPassword = getEnv("DB_PASSWORD", "YOUR_PASSWORD"); // REPLACE WITH ACTUAL CREDENTIALS
    } 

    private static class Holder {
        private static final MySQLConnect INSTANCE = new MySQLConnect();
    }

    public static MySQLConnect getInstance() {
        return Holder.INSTANCE;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                databaseLink,
                loginUsername,
                loginPassword
        );
    }

    // Environment variable reader
    private static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }
}
