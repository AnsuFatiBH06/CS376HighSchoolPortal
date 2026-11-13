package com.highschoolportal.database;

import java.sql.Connection;

// Temporary Class to check that the database connection works
// Delete once the connection works

public class ConnectionTest {

    public static void main(String[] args) {

        // Time out boundary
        java.sql.DriverManager.setLoginTimeout(10); // in seconds
        System.out.println("Trying to connect...");

        try (Connection conn = MySQLConnect.getInstance().getConnection()) {
            System.out.println("Connected: " + conn.getMetaData().getURL());

        } catch (Exception e) {
            System.out.println("Connection Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
