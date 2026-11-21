package com.highschoolportal.services;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.highschoolportal.model.Manager;
import com.highschoolportal.repository.TeacherRepository;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;

public class TeacherManagementService {
    private final TeacherRepository repository;

    public TeacherManagementService() {
        this(new TeacherRepository());
    }

    public TeacherManagementService(TeacherRepository repository) {
        this.repository = repository;
    }

    public TeacherRepository.CreatedTeacher createTeacher(
            Manager manager, String firstName, String lastName,
            String email, LocalDate dateOfBirth, char[] temporaryPassword)
            throws SQLException {

        if (manager == null || manager.getUserID() <= 0 || manager.getSchoolID() <= 0) {
            throw new IllegalArgumentException("An assigned school manager is required");
        }
        if (firstName == null || firstName.isBlank() || firstName.trim().length() > 100
                || lastName == null || lastName.isBlank() || lastName.trim().length() > 100) {
            throw new IllegalArgumentException("Enter valid teacher first and last names");
        }
        if (email == null || !email.trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
                || email.trim().length() > 255) {
            throw new IllegalArgumentException("Enter a valid teacher email address");
        }
        if (dateOfBirth == null || !dateOfBirth.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Enter a valid date of birth");
        }
        if (temporaryPassword == null || temporaryPassword.length < 12
                || temporaryPassword.length > 72) {
            throw new IllegalArgumentException("Temporary password must be 12 to 72 characters");
        }

        String hash = BCrypt.withDefaults().hashToString(12, temporaryPassword);
        return repository.createForManager(
                manager.getUserID(), firstName.trim(), lastName.trim(),
                email.trim(), dateOfBirth, hash);
    }

    public java.util.List<TeacherRepository.TeacherOption> getTeachers(Manager manager)
            throws SQLException {
        validateManager(manager);
        return repository.listTeachersForManager(manager.getUserID());
    }

    public java.util.List<TeacherRepository.SubjectOfferingOption> getSubjectOfferings(Manager manager)
            throws SQLException {
        validateManager(manager);
        return repository.listSubjectOfferingsForManager(manager.getUserID());
    }

    public void assignTeacher(Manager manager, int teacherID, int subjectAcademicYearID)
            throws SQLException {
        validateManager(manager);
        if (teacherID <= 0 || subjectAcademicYearID <= 0) {
            throw new IllegalArgumentException("Select a teacher and a subject offering");
        }
        repository.assignTeacherForManager(manager.getUserID(), teacherID, subjectAcademicYearID);
    }

    private void validateManager(Manager manager) {
        if (manager == null || manager.getUserID() <= 0 || manager.getSchoolID() <= 0) {
            throw new IllegalArgumentException("An assigned school manager is required");
        }
    }
}
