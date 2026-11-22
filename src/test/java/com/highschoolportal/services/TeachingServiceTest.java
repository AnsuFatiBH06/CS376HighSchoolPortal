package com.highschoolportal.services;

import static org.junit.jupiter.api.Assertions.assertNull;
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

    @Test
    void acceptsValidTeachingMaterialUpload() {

        assertNull(TeachingService.validateTeachingMaterialUpload(
                1,
                1,
                1,
                "lecture.pdf"
        ));
    }

    @Test
    void rejectsInvalidTeacherForTeachingMaterialUpload() {

        assertTrue(TeachingService.validateTeachingMaterialUpload(
                0,
                1,
                1,
                "lecture.pdf"
        ).contains("Teacher"));
    }

    @Test
    void rejectsInvalidSubjectForTeachingMaterialUpload() {

        assertTrue(TeachingService.validateTeachingMaterialUpload(
                1,
                0,
                1,
                "lecture.pdf"
        ).contains("subject"));
    }

    @Test
    void rejectsInvalidWeekForTeachingMaterialUpload() {

        assertTrue(TeachingService.validateTeachingMaterialUpload(
                1,
                1,
                0,
                "lecture.pdf"
        ).contains("week number"));
    }

    @Test
    void rejectsNonPdfTeachingMaterialUpload() {

        assertTrue(TeachingService.validateTeachingMaterialUpload(
                1,
                1,
                1,
                "lecture.docx"
        ).contains("PDF file"));
    }
}