package com.highschoolportal.repository;

import com.highschoolportal.database.MySQLConnect;
import com.highschoolportal.model.School;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SchoolRepository {

    public boolean insertPendingSchool(School school) throws SQLException {
        String sql =
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

        try(Connection connection = MySQLConnect.getInstance().getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, school.getSchoolName());
            statement.setString(2, school.getSchoolEmail());
            statement.setString(3, school.getSchoolEmailDomain());
            statement.setString(4, school.getSchoolAddress());

            return statement.executeUpdate() == 1;
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
                UPDATE School
                SET Status = 'Rejected'
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
