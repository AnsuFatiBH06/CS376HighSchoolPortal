package com.highschoolportal.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudentManagementServiceTest {

    @Test
    void acceptsValidStudentDetails() {

        assertNull(StudentManagementService.validateStudentEnrollmentDetails(
                "John",
                "Smith",
                "2008-05-10",
                1
        ));
    }

    @Test
    void rejectsBlankFirstName() {

        assertTrue(StudentManagementService.validateStudentEnrollmentDetails(
                " ",
                "Smith",
                "2008-05-10",
                1
        ).contains("first name"));
    }

    @Test
    void rejectsBlankLastName() {

        assertTrue(StudentManagementService.validateStudentEnrollmentDetails(
                "John",
                " ",
                "2008-05-10",
                1
        ).contains("last name"));
    }

    @Test
    void rejectsBlankDateOfBirth() {

        assertTrue(StudentManagementService.validateStudentEnrollmentDetails(
                "John",
                "Smith",
                " ",
                1
        ).contains("date of birth"));
    }

    @Test
    void rejectsInvalidDateOfBirthFormat() {

        assertTrue(StudentManagementService.validateStudentEnrollmentDetails(
                "John",
                "Smith",
                "10/05/2008",
                1
        ).contains("YYYY-MM-DD"));
    }

    @Test
    void rejectsInvalidAcademicYear() {

        assertTrue(StudentManagementService.validateStudentEnrollmentDetails(
                "John",
                "Smith",
                "2008-05-10",
                0
        ).contains("academic year"));
    }

    @Test
    void rejectsInvalidFirstName() {

        assertTrue(StudentManagementService.validateStudentEnrollmentDetails(
                "John123",
                "Smith",
                "2008-05-10",
                1
        ).contains("First name contains invalid characters"));
    }

    @Test
    void rejectsInvalidLastName() {

        assertTrue(StudentManagementService.validateStudentEnrollmentDetails(
                "John",
                "Smith123",
                "2008-05-10",
                1
        ).contains("Last name contains invalid characters"));
    }
}