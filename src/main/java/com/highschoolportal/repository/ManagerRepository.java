package com.highschoolportal.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.highschoolportal.database.MySQLConnect;
import com.highschoolportal.model.Manager;

public class ManagerRepository {
    public boolean requestSuperManagerStatus(Manager manager) throws SQLException {
        String sql =
        """
        UPDATE Manager
        SET SuperManagerStatus = 'pending'
        WHERE ManagerID = ?;
        """;

        try(Connection connection = MySQLConnect.getInstance().getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, manager.getUserID());

            return statement.executeUpdate() == 1;
        }
    }
}
