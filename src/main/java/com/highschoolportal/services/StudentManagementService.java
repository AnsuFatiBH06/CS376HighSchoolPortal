package com.highschoolportal.services;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
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

    
}
