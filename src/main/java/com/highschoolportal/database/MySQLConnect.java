package com.highschoolportal.database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnect {
    private static MySQLConnect singletonInstance;

    private final String databaseLink;
    private final String loginUsername;
    private final String loginPassword;

    private MySQLConnect() {
        this.databaseLink = "jdbc:mysql:devweb2026.cis.strath.ac.uk:3306";
        this.loginUsername = "root";
        this.loginPassword = "root";
    } // the values inside this constructor should definitely be changed

    public static MySQLConnect getInstance() {
        if (singletonInstance == null) {
            singletonInstance = new MySQLConnect();
        }

        return singletonInstance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                this.databaseLink,
                this.loginUsername,
                this.loginPassword
        );
    }
}
