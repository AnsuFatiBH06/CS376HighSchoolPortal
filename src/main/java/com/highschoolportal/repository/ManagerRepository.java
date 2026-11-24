package com.highschoolportal.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.highschoolportal.database.MySQLConnect;
import com.highschoolportal.model.Manager;
import com.highschoolportal.model.Manager.SuperManagerStatus;

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

    public List<Manager> findPendingSuperManagers() throws SQLException {
        String sql =
                """
                SELECT *
                FROM Manager
                WHERE SuperManagerStatus = 'pending'
                """;

        return getManagers(sql);
    }

    public List<Manager> findApprovedSuperManagers() throws SQLException {
        String sql =
                """
                SELECT *
                FROM Manager
                WHERE SuperManagerStatus = 'approved'
                """;

        return getManagers(sql);
    }

    public boolean approveSuperManager(int managerID) throws SQLException {
        String sql =
                """
                UPDATE Manager
                SET SuperManagerStatus = 'approved'
                WHERE ManagerID = ?
                """;

        try(Connection connection = MySQLConnect.getInstance().getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, managerID);

            return statement.executeUpdate() == 1;
        }
    }

    public boolean rejectSuperManager(int managerID) throws SQLException {
        String sql =
                """
                UPDATE Manager
                SET SuperManagerStatus = 'none'
                WHERE ManagerID = ?
                """;

        try(Connection connection = MySQLConnect.getInstance().getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, managerID);

            return statement.executeUpdate() == 1;
        }
    }

    private List<Manager> getManagers(String sql) throws SQLException {
        List<Manager> managers = new ArrayList<>();

        try(Connection connection = MySQLConnect.getInstance().getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery()) {

            while(result.next()) {
                Integer schoolId = (Integer) result.getObject("SchoolID");
                String superManagerStatus = result.getString("SuperManagerStatus");
                if (schoolId == null) {
                    managers.add(new Manager(
                                    result.getInt("ManagerID"),
                                    result.getString("FName"),
                                    result.getString("LName"),
                                    result.getString("Email"),
                                    result.getInt("CredentialsID"),
                                    SuperManagerStatus.valueOf(superManagerStatus.toUpperCase())
                                )
                    );
                } else {
                    managers.add(new Manager(
                                    result.getInt("ManagerID"),
                                    result.getString("FName"),
                                    result.getString("LName"),
                                    result.getString("Email"),
                                    result.getInt("CredentialsID"),
                                    SuperManagerStatus.valueOf(superManagerStatus.toUpperCase()),
                                    schoolId
                                )
                    );
                }
            }
        }
        return managers;
    }
}
