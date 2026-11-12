package com.highschoolportal.repository;

import com.highschoolportal.database.MySQLConnect;
import com.highschoolportal.model.AcademicYear;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AcademicYearRepository {

    public boolean isSchoolApproved(int schoolID) throws SQLException {
        String sql = "SELECT Status FROM School WHERE SchoolID = ?";

        try (Connection connection = MySQLConnect.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, schoolID);

            try (ResultSet result = statement.executeQuery()) {
                return result.next()
                        && "Approved".equalsIgnoreCase(result.getString("Status"));
            }
        }
    }

    public boolean existsBySchoolAndName(int schoolID, String name)
            throws SQLException {

        String sql = """
                SELECT 1
                FROM AcademicYear
                WHERE SchoolID = ? AND Name = ?
                LIMIT 1
                """;

        try (Connection connection = MySQLConnect.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, schoolID);
            statement.setString(2, name);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public AcademicYear create(AcademicYear academicYear) throws SQLException {

        String sql = """
                INSERT INTO AcademicYear
                (SchoolID, Name, StartDate, EndDate,
                 MinOptionalSubjects, MaxOptionalSubjects,
                 RequiredPassedSubjects, ExamPeriodOpen)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = MySQLConnect.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, academicYear.getSchoolID());
            statement.setString(2, academicYear.getName());
            statement.setObject(3, academicYear.getStartDate());
            statement.setObject(4, academicYear.getEndDate());
            statement.setInt(5, academicYear.getMinOptionalSubjects());
            statement.setInt(6, academicYear.getMaxOptionalSubjects());
            statement.setInt(7, academicYear.getRequiredPassedSubjects());
            statement.setBoolean(8, false);

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException(
                            "The academic year was saved, but its generated ID was not returned."
                    );
                }

                return new AcademicYear(
                        keys.getInt(1),
                        academicYear.getSchoolID(),
                        academicYear.getName(),
                        academicYear.getStartDate(),
                        academicYear.getEndDate(),
                        academicYear.getMinOptionalSubjects(),
                        academicYear.getMaxOptionalSubjects(),
                        academicYear.getRequiredPassedSubjects(),
                        false
                );
            }
        }
    }

    public List<AcademicYear> findBySchoolID(int schoolID) throws SQLException {

        String sql = """
                SELECT AcademicYearID, SchoolID, Name, StartDate, EndDate,
                       MinOptionalSubjects, MaxOptionalSubjects,
                       RequiredPassedSubjects, ExamPeriodOpen
                FROM AcademicYear
                WHERE SchoolID = ?
                ORDER BY StartDate DESC, AcademicYearID DESC
                """;

        List<AcademicYear> years = new ArrayList<>();

        try (Connection connection = MySQLConnect.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, schoolID);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    years.add(mapRow(result));
                }
            }
        }

        return years;
    }

    public Optional<AcademicYear> findByIDAndSchoolID(
            int academicYearID, int schoolID) throws SQLException {

        String sql = """
                SELECT AcademicYearID, SchoolID, Name, StartDate, EndDate,
                       MinOptionalSubjects, MaxOptionalSubjects,
                       RequiredPassedSubjects, ExamPeriodOpen
                FROM AcademicYear
                WHERE AcademicYearID = ? AND SchoolID = ?
                """;

        try (Connection connection = MySQLConnect.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, academicYearID);
            statement.setInt(2, schoolID);

            try (ResultSet result = statement.executeQuery()) {
                return result.next()
                        ? Optional.of(mapRow(result))
                        : Optional.empty();
            }
        }
    }

    public int countOptionalSubjects(int academicYearID) throws SQLException {
        return countSubjects(academicYearID, true);
    }

    public int countSubjects(int academicYearID) throws SQLException {
        return countSubjects(academicYearID, false);
    }

    private int countSubjects(int academicYearID, boolean optionalOnly)
            throws SQLException {

        String sql = optionalOnly
                ? """
                  SELECT COUNT(*)
                  FROM SubjectAcademicYear
                  WHERE AcademicYearID = ? AND IsCore = FALSE
                  """
                : """
                  SELECT COUNT(*)
                  FROM SubjectAcademicYear
                  WHERE AcademicYearID = ?
                  """;

        try (Connection connection = MySQLConnect.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, academicYearID);

            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        }
    }

    public boolean updateRules(
            int academicYearID,
            int schoolID,
            int minOptionalSubjects,
            int maxOptionalSubjects,
            int requiredPassedSubjects) throws SQLException {

        String sql = """
                UPDATE AcademicYear
                SET MinOptionalSubjects = ?,
                    MaxOptionalSubjects = ?,
                    RequiredPassedSubjects = ?
                WHERE AcademicYearID = ? AND SchoolID = ?
                """;

        try (Connection connection = MySQLConnect.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, minOptionalSubjects);
            statement.setInt(2, maxOptionalSubjects);
            statement.setInt(3, requiredPassedSubjects);
            statement.setInt(4, academicYearID);
            statement.setInt(5, schoolID);

            return statement.executeUpdate() == 1;
        }
    }

    private AcademicYear mapRow(ResultSet result) throws SQLException {

        java.sql.Date sqlStartDate = result.getDate("StartDate");
        java.sql.Date sqlEndDate = result.getDate("EndDate");

        LocalDate startDate = sqlStartDate == null
                ? null : sqlStartDate.toLocalDate();

        LocalDate endDate = sqlEndDate == null
                ? null : sqlEndDate.toLocalDate();

        return new AcademicYear(
                result.getInt("AcademicYearID"),
                result.getInt("SchoolID"),
                result.getString("Name"),
                startDate,
                endDate,
                result.getInt("MinOptionalSubjects"),
                result.getInt("MaxOptionalSubjects"),
                result.getInt("RequiredPassedSubjects"),
                result.getBoolean("ExamPeriodOpen")
        );
    }
}