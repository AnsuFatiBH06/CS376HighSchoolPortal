package com.highschoolportal.services;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import com.highschoolportal.database.MySQLConnect;
import com.highschoolportal.model.StudentEnrollmentRequest;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class StudentManagementService {

    // Creates a new student and enrolls the student in the selected academic year
    public int createStudentAndEnroll(int managerID, StudentEnrollmentRequest request)
    throws SQLException {

        // Ensure that the manager entered valid details
        validateRequest(request);

        // Find the logged-in manager's school
        int schoolID = findManagerSchoolID(managerID);

        // Find the school's email domain
        String emailDomain = findSchoolEmailDomain(schoolID);

        // Generate the student's school email
        String email = generateStudentEmail(
            request.getFirstName(),
            request.getLastName(),
            emailDomain
        );

        // Use the student's date of birth as the temporary password
        String temporaryPassword = request.getDateOfBirth();

        // Hash the password before storing
        String passwordHash = BCrypt.withDefaults().hashToString(12, temporaryPassword.toCharArray());

        // Convert the date of birth into an SQL date
        Date dateOfBirth = Date.valueOf(LocalDate.parse(request.getDateOfBirth()));

        try (Connection connection = MySQLConnect.getInstance().getConnection()) {

            // In case we need to revert
            connection.setAutoCommit(false);

            // make sure the selected academic year belongs to the manager's school
            try {
                ensureAcademicYearBelongsToSchool(
                    connection,
                    request.getAcademicYearID(),
                    schoolID
                );

                // Create the student's login credentials
                int credentialsID = createStudentCredentials(connection, passwordHash);

                // Create the student record
                int studentID = createStudent(
                    connection,
                    request.getFirstName(),
                    request.getLastName(),
                    email,
                    dateOfBirth,
                    schoolID,
                    credentialsID
                );

                // Enroll the student in the selected academic year
                int studentAcademicYearID = createStudentAcademicYear(
                    connection,
                    studentID,
                    request.getAcademicYearID()
                );

                // Add all core subjects to the student's curriculum
                addCoreSubjectsToCurriculum(
                    connection,
                    studentAcademicYearID,
                    request.getAcademicYearID(),
                    request.getFirstName() + " " + request.getLastName(),
                    findAcademicYearName(connection, request.getAcademicYearID())
                );

                // Save all database changes
                connection.commit();

                // Return the new student's ID
                return studentID;

            } catch (Exception e) {

                // Undo all database changes if any part of the process fails
                connection.rollback();

                if (e instanceof SQLException sqlException) {
                    throw sqlException;
                }

                throw new SQLException("Student account creation failed", e);

            } finally {
                // Restore the automaitc committing
                connection.setAutoCommit(true);
            }
        }
    }

    // Validates the student details inputted by the manager
    private void validateRequest(StudentEnrollmentRequest request) {

        // The request itself must exist
        if (request == null) {
            throw new IllegalArgumentException("Student details are required");
        }

        // The first name cannot be empty
        if (request.getFirstName() == null || request.getFirstName().isBlank()) {
            throw new IllegalArgumentException("Enter the student's first name");
        }

        // The last name also cannot be empty
        if (request.getLastName() == null || request.getLastName().isBlank()) {
            throw new IllegalArgumentException("Enter the student's last name");
        }

        // The date of birth also also cannot be empty
        if (request.getDateOfBirth()== null || request.getLastName().isBlank()) {
            throw new IllegalArgumentException("Enter the student's date of birth");
        }

        // Date must follow the proper YYYY-MM-DD format
        try {
            LocalDate.parse(request.getDateOfBirth()); 

        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                "Enter the date of birth in YYYY-MM-DD format");
        }

        // Ensure that the academic year is valid
        if (request.getAcademicYearID() <= 0) {
            throw new IllegalArgumentException("Select an academic year");
        }
    }

    // Finds the school ID linked to the logged-in manager
    private int findManagerSchoolID(int managerID) throws SQLException {

        String sql = "SELECT SchoolID FROM Manager WHERE ManagerID = ?";

        try (Connection connection = MySQLConnect.getInstance().getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, managerID);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next() || result.getObject("SchoolID") == null) {
                    throw new IllegalArgumentException(
                        "The logged-in Manager is not linked to a school");
                }

                return result.getInt("SchoolID");
            }
        }
    } 

    // Finds the email domain belonging to the Manager's school
    private String findSchoolEmailDomain(int schoolID) throws SQLException {

        String sql = "SELECT EmailDomain FROM School WHERE SchoolID = ?";

        try (Connection connection = MySQLConnect.getInstance().getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, schoolID);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalArgumentException("The school was not found");
                }

                String emailDomain = result.getString("EmailDomain");

                if (emailDomain == null || emailDomain.isBlank()) {
                    throw new IllegalArgumentException(
                        "The school does not have an email domain configured");
                }

                return emailDomain.trim();
            }

        }
    }

    /** Generates a student email address using the school's email domain
     * "John Shimmian", "delrey.com"
     * returns the "john.shimmian@delrey.com" generated email
     */ 
    private String generatedStudentEmail(String firstName, String lastName, String emailDomain) {

        // Remove spaces and convert the names to lowercase
        String cleanedFirstName = firstName.trim().toLowerCase().replaceAll("\\s+", "");
        String cleanedLastName = lastName.trim().toLowerCase().replaceAll("\\s+", "");

        // Build the email address
        String email = cleanedFirstName + "." + cleanedLastName + "@" + emailDomain;

        // Ensure it follows the valid format
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("The school email domain is invalid");
        }

        return email;
    }

    // Checks that the selected academic year belongs to the manager's school
    private void ensureAcademicYearBelongsToSchool(Connection connection, int academicYearID, int schoolID) throws SQLException {

        String sql = """
        SELECT 1
        FROM AcademicYear
        WHERE AcademicYearID = ? AND SchoolID = ?
        """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, academicYearID);
            statement.setInt(2, schoolID);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalArgumentException(
                        "The selected academic year does not belong to your school");
                }
            }
        }
    }

    // Creates a student credentials record
    private int createStudentCredentials(Connection connection, String passwordHash)
            throws SQLException {

            String sql = """
                INSERT INTO Credentials
                (PasswordHash, Role, MustChangePassword, Status)
                VALUES (?, 'Student', TRUE, 'Active')
                """;

        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, passwordHash);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Student credentials could not be created.");
                }

                return keys.getInt(1);
            }
        }
    }

    // Creates a student record
    private int createStudent(Connection connection, String firstName, String lastName,
                              String email, Date dateOfBirth, int schoolID,
                              int credentialsID) throws SQLException {

        String sql = """
                INSERT INTO Student
                (FName, LName, Email, DOB, SchoolID, CredentialsID)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, firstName.trim());
            statement.setString(2, lastName.trim());
            statement.setString(3, email);
            statement.setDate(4, dateOfBirth);
            statement.setInt(5, schoolID);
            statement.setInt(6, credentialsID);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Student account could not be created.");
                }

                return keys.getInt(1);
            }
        }
        
        // Enrolls the student in the selected academic year
        private int createStudentAcademicYear(Connection connection, int studentID,
                                          int academicYearID) throws SQLException {

        String sql = """
                INSERT INTO StudentAcademicYear
                (StudentID, AcademicYearID, Grade, CertificateStatus)
                VALUES (?, ?, NULL, 'Not Qualified')
                """;

        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, studentID);
            statement.setInt(2, academicYearID);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Student enrolment could not be created.");
                }

                return keys.getInt(1);
            }
        }
    }

    

}
