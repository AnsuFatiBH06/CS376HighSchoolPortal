package com.highschoolportal.database;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnect {

    // JDBC URL (jdbc:mysql://host:3306/database)
    private final String databaseLink;

    // SQL Account Credentials
    private final String loginUsername;
    private final String loginPassword;

    private static final Dotenv dotenv = Dotenv.load();

    private MySQLConnect() {
        String host = dotenv.get("DB_HOST");
        String port = dotenv.get("DB_PORT");
        String name = dotenv.get("DB_NAME"); 

        // Build the JDBC URL from the parts 
        // WILL FAIL IF .env DOES NOT CONTAIN ALL VALUES
        this.databaseLink = "jdbc:mysql://" + host + ":" + port + "/" + name;
        this.loginUsername = dotenv.get("DB_USER"); 
        this.loginPassword = dotenv.get("DB_PASSWORD");
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
}
