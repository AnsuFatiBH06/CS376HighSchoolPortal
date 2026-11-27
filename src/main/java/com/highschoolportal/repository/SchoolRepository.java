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

        String schoolSql = """
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

        try {
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
            String managerSql = """
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
        String sql = """
                SELECT *
                FROM School
                WHERE Status = 'Approved'
                """;

        return getSchools(sql);
    }

    public List<School> findPendingSchools() throws SQLException {
        String sql = """
                SELECT *
                FROM School
                WHERE Status = 'Pending'
                """;

        return getSchools(sql);
    }

    public boolean approveSchool(int schoolID) throws SQLException {
        String sql = """
                UPDATE School
                SET Status = 'Approved'
                WHERE SchoolID = ?
                """;

        try (Connection connection = MySQLConnect.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, schoolID);

            return statement.executeUpdate() == 1;
        }
    }

    public boolean rejectSchool(int schoolID) throws SQLException {
        String sql = """
                DELETE FROM School
                WHERE SchoolID = ?
                """;

        try (Connection connection = MySQLConnect.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, schoolID);

            return statement.executeUpdate() == 1;
        }
    }

    public boolean deleteSchoolAndRetainManager(
            int schoolID,
            int managerID) throws SQLException {

        String findSchoolSql = """
                SELECT SchoolID
                FROM Manager
                WHERE ManagerID = ?
                  AND SchoolID = ?
                FOR UPDATE
                """;

        try (Connection connection = MySQLConnect.getInstance().getConnection()) {

            connection.setAutoCommit(false);

            try {
                // Confirm that this Manager owns the school.
                try (PreparedStatement statement = connection.prepareStatement(findSchoolSql)) {

                    statement.setInt(1, managerID);
                    statement.setInt(2, schoolID);

                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            connection.rollback();
                            return false;
                        }
                    }
                }

                // Delete notifications associated with this school's students.
                executeUpdate(connection, """
                        DELETE sn
                        FROM StudentNotification sn
                        JOIN Student s ON s.StudentID = sn.StudentID
                        WHERE s.SchoolID = ?
                        """, schoolID);

                // Delete attendance records associated with student details.
                executeUpdate(connection, """
                        DELETE a
                        FROM Attendance a
                        JOIN Details d ON d.DetailID = a.DetailID
                        JOIN StudentAcademicYear say
                            ON say.StudentAcademicYearID = d.StudentAcademicYearID
                        JOIN Student s ON s.StudentID = say.StudentID
                        WHERE s.SchoolID = ?
                        """, schoolID);

                // Delete exam marks associated with student details.
                executeUpdate(connection, """
                        DELETE em
                        FROM ExamMark em
                        JOIN Details d ON d.DetailID = em.DetailID
                        JOIN StudentAcademicYear say
                            ON say.StudentAcademicYearID = d.StudentAcademicYearID
                        JOIN Student s ON s.StudentID = say.StudentID
                        WHERE s.SchoolID = ?
                        """, schoolID);

                // Delete teaching materials uploaded by the school's teachers.
                executeUpdate(connection, """
                        DELETE tm
                        FROM TeachingMaterial tm
                        JOIN Teacher t ON t.TeacherID = tm.TeacherID
                        WHERE t.SchoolID = ?
                        """, schoolID);

                // Delete teaching materials belonging to the school's offerings.
                executeUpdate(connection, """
                        DELETE tm
                        FROM TeachingMaterial tm
                        JOIN SubjectAcademicYear say
                            ON say.SubjectAcademicYearID =
                               tm.SubjectAcademicYearID
                        JOIN AcademicYear ay
                            ON ay.AcademicYearID = say.AcademicYearID
                        WHERE ay.SchoolID = ?
                        """, schoolID);

                // Delete exams belonging to the school's subject offerings.
                executeUpdate(connection, """
                        DELETE e
                        FROM Exam e
                        JOIN SubjectAcademicYear say
                            ON say.SubjectAcademicYearID = e.SubjectAcademicYearID
                        JOIN AcademicYear ay
                            ON ay.AcademicYearID = say.AcademicYearID
                        WHERE ay.SchoolID = ?
                        """, schoolID);

                // Delete student subject details.
                executeUpdate(connection, """
                        DELETE d
                        FROM Details d
                        JOIN StudentAcademicYear say
                            ON say.StudentAcademicYearID =
                               d.StudentAcademicYearID
                        JOIN Student s ON s.StudentID = say.StudentID
                        WHERE s.SchoolID = ?
                        """, schoolID);

                // Also remove details for offerings belonging to this school.
                executeUpdate(connection, """
                        DELETE d
                        FROM Details d
                        JOIN SubjectAcademicYear offer
                            ON offer.SubjectAcademicYearID =
                               d.SubjectAcademicYearID
                        JOIN AcademicYear ay
                            ON ay.AcademicYearID = offer.AcademicYearID
                        WHERE ay.SchoolID = ?
                        """, schoolID);

                // Delete student academic-year enrolments.
                executeUpdate(connection, """
                        DELETE say
                        FROM StudentAcademicYear say
                        JOIN Student s ON s.StudentID = say.StudentID
                        WHERE s.SchoolID = ?
                        """, schoolID);

                // Delete subject offerings for this school's academic years.
                executeUpdate(connection, """
                        DELETE offer
                        FROM SubjectAcademicYear offer
                        JOIN AcademicYear ay
                            ON ay.AcademicYearID = offer.AcademicYearID
                        WHERE ay.SchoolID = ?
                        """, schoolID);

                // Disable student credentials before deleting student profiles.
                executeUpdate(connection, """
                        UPDATE Credentials c
                        JOIN Student s ON s.CredentialsID = c.CredentialsID
                        SET c.Status = 'Inactive'
                        WHERE s.SchoolID = ?
                        """, schoolID);

                // Disable teacher credentials before deleting teacher profiles.
                executeUpdate(connection, """
                        UPDATE Credentials c
                        JOIN Teacher t ON t.CredentialsID = c.CredentialsID
                        SET c.Status = 'Inactive'
                        WHERE t.SchoolID = ?
                        """, schoolID);

                // Delete student profiles.
                executeUpdate(connection, """
                        DELETE FROM Student
                        WHERE SchoolID = ?
                        """, schoolID);

                // Delete teacher profiles.
                executeUpdate(connection, """
                        DELETE FROM Teacher
                        WHERE SchoolID = ?
                        """, schoolID);

                // Delete academic years.
                executeUpdate(connection, """
                        DELETE FROM AcademicYear
                        WHERE SchoolID = ?
                        """, schoolID);

                // Delete subjects.
                executeUpdate(connection, """
                        DELETE FROM Subject
                        WHERE SchoolID = ?
                        """, schoolID);

                // Detach the Manager without deleting their account.
                executeUpdate(connection, """
                        UPDATE Manager
                        SET SchoolID = NULL
                        WHERE ManagerID = ?
                          AND SchoolID = ?
                        """, managerID, schoolID);

                // Delete the school last.
                int deletedSchools = executeUpdate(connection, """
                        DELETE FROM School
                        WHERE SchoolID = ?
                        """, schoolID);

                if (deletedSchools != 1) {
                    throw new SQLException(
                            "The school could not be deleted.");
                }

                connection.commit();
                return true;

            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private int executeUpdate(
            Connection connection,
            String sql,
            int... parameters) throws SQLException {

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            for (int i = 0; i < parameters.length; i++) {
                statement.setInt(i + 1, parameters[i]);
            }

            return statement.executeUpdate();
        }
    }

    private List<School> getSchools(String sql) throws SQLException {
        List<School> schools = new ArrayList<>();

        try (Connection connection = MySQLConnect.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                schools.add(new School(
                        result.getInt("SchoolID"),
                        result.getString("Name"),
                        result.getString("Email"),
                        result.getString("EmailDomain"),
                        result.getString("Address"),
                        result.getString("Status")));
            }
        }
        return schools;
    }
}
