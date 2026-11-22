package com.highschoolportal.services;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class TeachingServiceTest {

    @Test
    void rejectsInvalidTeacherIDForEnrolledStudents() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new TeachingService().getEnrolledStudents(0, 1)
        );

        assertTrue(exception.getMessage().contains("Teacher"));
    }

    @Test
    void rejectsInvalidSubjectForEnrolledStudents() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new TeachingService().getEnrolledStudents(1, 0)
        );

        assertTrue(exception.getMessage().contains("subject"));
    }
}