package com.highschoolportal.repository;

import com.highschoolportal.database.MySQLConnect;
import com.highschoolportal.model.Manager;
import com.highschoolportal.model.School;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SchoolRepository {
    public boolean insertPendingSchool(School school, Manager manager) throws SQLException {
        Connection connection = null;

        String schoolSql =
                """
                INSERT INTO School
                (
                    Name,
                    Email,
                    EmailDomain,
                    Address,
                    Status
                )
                VALUES
                (
                    ?, ?, ?, ?, 'Pending'
                )
                """;

        try{
            connection = MySQLConnect.getInstance().getConnection();
            PreparedStatement statement = connection.prepareStatement(schoolSql, Statement.RETURN_GENERATED_KEYS);

            connection.setAutoCommit(false);

            statement.setString(1, school.getSchoolName());
            statement.setString(2, school.getSchoolEmail());
            statement.setString(3, school.getSchoolEmailDomain());
            statement.setString(4, school.getSchoolAddress());

            int schoolInsertSuccess = statement.executeUpdate(); // should be 1

            if (schoolInsertSuccess != 1) {
                connection.rollback();
                return false;
            }

            // Retrieve school id to assign to manager table
            ResultSet generatedKeys = statement.getGeneratedKeys();

            if (!generatedKeys.next()) {
                connection.rollback();
                return false;
            }

            int schoolID = generatedKeys.getInt(1);


            // Assign to manager now
            String managerSql = 
                """
                UPDATE Manager
                SET SchoolID = ?
                WHERE ManagerID = ?
                """;
            
            PreparedStatement managerStatement = connection.prepareStatement(managerSql);

            managerStatement.setInt(1, schoolID);

            managerStatement.setInt(2, manager.getUserID());

            int updatedManagerRow = managerStatement.executeUpdate();

            if (updatedManagerRow != 1) {
                connection.rollback();
                return false;
            }

            // If successful
            connection.commit();
            return true;
        } catch (SQLException exception) {
            if (connection != null) {
                connection.rollback();
            }

            throw exception;
        } finally {
            if (connection != null) {
                connection.setAutoCommit(true);
                connection.close();
            }
        }
    }

    public List<School> findApprovedSchools() throws SQLException {
        String sql =
                """
                SELECT *
                FROM School
                WHERE Status = 'Approved'
                """;

        return getSchools(sql);
    }

    public List<School> findPendingSchools() throws SQLException {
        String sql =
                """
                SELECT *
                FROM School
                WHERE Status = 'Pending'
                """;

        return getSchools(sql);
    }

    public boolean approveSchool(int schoolID) throws SQLException {
        String sql =
                """
                UPDATE School
                SET Status = 'Approved'
                WHERE SchoolID = ?
                """;

        try(Connection connection = MySQLConnect.getInstance().getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, schoolID);

            return statement.executeUpdate() == 1;
        }
    }

    public boolean rejectSchool(int schoolID) throws SQLException {
        String sql =
                """
                DELETE FROM School
                WHERE SchoolID = ?
                """;

        try(Connection connection = MySQLConnect.getInstance().getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, schoolID);

            return statement.executeUpdate() == 1;
        }
    }

    private List<School> getSchools(String sql) throws SQLException {
        List<School> schools = new ArrayList<>();

        try(Connection connection = MySQLConnect.getInstance().getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery()) {

            while(result.next()) {
                schools.add(new School(
                                result.getInt("SchoolID"),
                                result.getString("Name"),
                                result.getString("Email"),
                                result.getString("EmailDomain"),
                                result.getString("Address"),
                                result.getString("Status")
                            )
                );
            }
        }
        return schools;
    }
}
