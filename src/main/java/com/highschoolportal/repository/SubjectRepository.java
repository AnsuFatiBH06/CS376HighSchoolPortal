package com.highschoolportal.repository;

import com.highschoolportal.database.MySQLConnect;
import com.highschoolportal.model.Subject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SubjectRepository {

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
                FROM Subject
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

    public Subject create(Subject subject) throws SQLException {

        String sql = """
                INSERT INTO Subject
                (Name, Code, IsCore,
                 PassMark, MaxMark,
                 ExamSeriesNumber, SchoolID)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = MySQLConnect.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, subject.getSubjectName());
            statement.setString(2, subject.getSubjectCode());
            statement.setBoolean(3, subject.getIsCore());
            statement.setFloat(4, subject.getPassMark());
            statement.setFloat(5, subject.getMaxMark());
            statement.setInt(6, subject.getExamSeriesNumber());
            statement.setInt(7, subject.getSchoolID());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException(
                            "The subject was saved, but its generated ID was not returned."
                    );
                }

                return new Subject(
                        keys.getInt(1),
                        subject.getSubjectName(),
                        subject.getSubjectCode(),
                        subject.getIsCore(),
                        subject.getPassMark(),
                        subject.getMaxMark(),
                        subject.getExamSeriesNumber(),
                        subject.getSchoolID()
                );
            }
        }
    }

    public List<Subject> findBySchoolID(int schoolID) throws SQLException {

        String sql = """
                SELECT SubjectID, Name, Code, IsCore,
                       PassMark, MaxMark, ExamSeriesNumber
                FROM Subject
                WHERE SchoolID = ?
                ORDER BY Name DESC, SubjectID DESC
                """;

        List<Subject> subjects = new ArrayList<>();

        try (Connection connection = MySQLConnect.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, schoolID);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    subjects.add(mapRow(result));
                }
            }
        }

        return subjects;
    }

    public Optional<Subject> findByIDAndSchoolID(int subjectID, int schoolID) throws SQLException {

        String sql = """
                SELECT SubjectID, Name, Code, IsCore,
                       PassMark, MaxMark, ExamSeriesNumber
                FROM Subject
                WHERE SubjectID = ? AND SchoolID = ?
                """;

        try (Connection connection = MySQLConnect.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, subjectID);
            statement.setInt(2, schoolID);

            try (ResultSet result = statement.executeQuery()) {
                return result.next()
                        ? Optional.of(mapRow(result))
                        : Optional.empty();
            }
        }
    }

    // public int countOptionalSubjects(int academicYearID) throws SQLException {
    //     return countSubjects(academicYearID, true);
    // }

    // public int countSubjects(int academicYearID) throws SQLException {
    //     return countSubjects(academicYearID, false);
    // }

    // private int countSubjects(int academicYearID, boolean optionalOnly)
    //         throws SQLException {

    //     String sql = optionalOnly
    //             ? """
    //               SELECT COUNT(*)
    //               FROM SubjectAcademicYear
    //               WHERE AcademicYearID = ? AND IsCore = FALSE
    //               """
    //             : """
    //               SELECT COUNT(*)
    //               FROM SubjectAcademicYear
    //               WHERE AcademicYearID = ?
    //               """;

    //     try (Connection connection = MySQLConnect.getInstance().getConnection();
    //          PreparedStatement statement = connection.prepareStatement(sql)) {

    //         statement.setInt(1, academicYearID);

    //         try (ResultSet result = statement.executeQuery()) {
    //             result.next();
    //             return result.getInt(1);
    //         }
    //     }
    // }

    // public boolean updateRules(
    //         int academicYearID,
    //         int schoolID,
    //         int minOptionalSubjects,
    //         int maxOptionalSubjects,
    //         int requiredPassedSubjects) throws SQLException {

    //     String sql = """
    //             UPDATE AcademicYear
    //             SET MinOptionalSubjects = ?,
    //                 MaxOptionalSubjects = ?,
    //                 RequiredPassedSubjects = ?
    //             WHERE AcademicYearID = ? AND SchoolID = ?
    //             """;

    //     try (Connection connection = MySQLConnect.getInstance().getConnection();
    //          PreparedStatement statement = connection.prepareStatement(sql)) {

    //         statement.setInt(1, minOptionalSubjects);
    //         statement.setInt(2, maxOptionalSubjects);
    //         statement.setInt(3, requiredPassedSubjects);
    //         statement.setInt(4, academicYearID);
    //         statement.setInt(5, schoolID);

    //         return statement.executeUpdate() == 1;
    //     }
    // }

    private Subject mapRow(ResultSet result) throws SQLException {

        // java.sql.Date sqlStartDate = result.getDate("StartDate");
        // java.sql.Date sqlEndDate = result.getDate("EndDate");

        // LocalDate startDate = sqlStartDate == null
        //         ? null : sqlStartDate.toLocalDate();

        // LocalDate endDate = sqlEndDate == null
        //         ? null : sqlEndDate.toLocalDate();

        return new Subject(
                result.getInt("SubjectID"),
                result.getString("Name"),
                result.getString("Code"),
                result.getBoolean("IsCore"),
                result.getFloat("PassMark"),
                result.getFloat("MaxMark"),
                result.getInt("ExamSeriesNumber"),
                result.getInt("SchoolID")
        );
    }
}