package com.highschoolportal.services;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

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

        @Test
    void rejectsInvalidSubjectCodeFilter() {

        assertTrue(StudentManagementService.validateStudentFilters(
                "MATHS123!",
                null
        ).contains("Subject code contains invalid characters"));
    }

    @Test
    void rejectsInvalidCertificateStatusFilter() {

        assertTrue(StudentManagementService.validateStudentFilters(
                null,
                "Passed!"
        ).contains("Certificate status contains invalid characters"));
    }

    @Test
    void rejectsInvalidManagerIDForStudentList() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new StudentManagementService().getAllStudents(0)
        );

        assertTrue(exception.getMessage().contains("Manager"));
    }

    @Test
    void rejectsInvalidManagerIDForIncompleteCurricula() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new StudentManagementService()
                        .getStudentsWithIncompleteCurricula(0)
        );

        assertTrue(exception.getMessage().contains("Manager"));
    }

    @Test
    void acceptsValidNotification() {

        assertNull(StudentManagementService.validateNotification(
                1,
                1,
                "Please select your missing optional subjects."
        ));
    }

    @Test
    void rejectsInvalidManagerForNotification() {

        assertTrue(StudentManagementService.validateNotification(
                0,
                1,
                "Please select your missing optional subjects."
        ).contains("School Manager"));
    }

    @Test
    void rejectsInvalidStudentForNotification() {

        assertTrue(StudentManagementService.validateNotification(
                1,
                0,
                "Please select your missing optional subjects."
        ).contains("student"));
    }

    @Test
    void rejectsBlankNotificationMessage() {

        assertTrue(StudentManagementService.validateNotification(
                1,
                1,
                " "
        ).contains("notification message"));
    }

    @Test
    void rejectsLongNotificationMessage() {

        assertTrue(StudentManagementService.validateNotification(
                1,
                1,
                "a".repeat(501)
        ).contains("500 characters or fewer"));
    }
}