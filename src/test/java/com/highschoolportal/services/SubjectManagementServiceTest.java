package com.highschoolportal.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubjectManagementServiceTest {

    @Test
    void acceptsValidSchoolDetails() {

        assertNull(SubjectManagementService.validateSubject(
                "Logic & Algorithms",
                "CS282",
                true,
                40,
                100,
                1
        ));
    }

    @Test
    void rejectsBlankName() {

        assertTrue(SubjectManagementService.validateSubject(
                " ",
                "CS282",
                true,
                40,
                100,
                1
        ).contains("Subject Name"));
    }

    @Test
    void rejectsBlankCode() {

        assertTrue(SubjectManagementService.validateSubject(
                "Logic & Algorithms",
                " ",
                true,
                40,
                100,
                1
        ).contains("Subject Code"));
    }

    @Test
    void rejectsShortName() {

        assertTrue(SubjectManagementService.validateSubject(
                "LL",
                "CS282",
                true,
                40,
                100,
                1
        ).contains("between 3 and 45 characters"));
    }

    @Test
    void rejectsLongName() {

        assertTrue(SubjectManagementService.validateSubject(
                "Logic & AlgorithmsLogic & AlgorithmsLogic & AlgorithmsLogic & AlgorithmsLogic & AlgorithmsLogic & AlgorithmsLogic & AlgorithmsLogic & Algorithms",
                "CS282",
                true,
                40,
                100,
                1
        ).contains("between 3 and 45 characters"));
    }

    @Test
    void rejectsInvalidName() {

        assertTrue(SubjectManagementService.validateSubject(
                "@@@@@###",
                "CS282",
                true,
                40,
                100,
                1
        ).contains("valid subject name"));
    }

    @Test
    void rejectsShortCode() {

        assertTrue(SubjectManagementService.validateSubject(
                "Logic & Algorithms",
                "CS",
                true,
                40,
                100,
                1
        ).contains("between 3 and 15 characters"));
    }

    @Test
    void rejectsLongCode() {

        assertTrue(SubjectManagementService.validateSubject(
                "Logic & Algorithms",
                "CS282CS282CS282CS282CS282CS282CS282CS282CS282CS282CS282CS282CS282CS282",
                true,
                40,
                100,
                1
        ).contains("between 3 and 15 characters"));
    }

    @Test
    void rejectsInvalidCode() {

        assertTrue(SubjectManagementService.validateSubject(
                "Logic & Algorithms",
                "ccccccc",
                true,
                40,
                100,
                1
        ).contains("uppercase letters, numbers, and hyphens"));
    }

    @Test
    void rejectsNegativePassMark() {

        assertTrue(SubjectManagementService.validateSubject(
                "Logic & Algorithms",
                "CS282",
                true,
                -50,
                100,
                1
        ).contains("pass mark cannot be negative"));
    }

    @Test
    void rejectsNegativeMaxMark() {

        assertTrue(SubjectManagementService.validateSubject(
                "Logic & Algorithms",
                "CS282",
                true,
                40,
                -100,
                1
        ).contains("maximum mark cannot be negative"));
    }

    @Test
    void acceptsPassMarkEqualToMaxMark() {

        assertNull(SubjectManagementService.validateSubject(
                "Logic & Algorithms",
                "CS282",
                true,
                60,
                60,
                1
        ));
    }

    @Test
    void rejectsPassMarkGreaterThanMaxMark() {

        assertTrue(SubjectManagementService.validateSubject(
                "Logic & Algorithms",
                "CS282",
                true,
                90,
                50,
                1
        ).contains("pass mark cannot exceed the maximum mark"));
    }

    @Test
    void acceptsFourExams() {

        assertNull(SubjectManagementService.validateSubject(
                "Logic & Algorithms",
                "CS282",
                true,
                40,
                100,
                4
        ));
    }

    @Test
    void rejectsZeroExams() {

        assertTrue(SubjectManagementService.validateSubject(
                "Logic & Algorithms",
                "CS282",
                true,
                40,
                100,
                0
        ).contains("should be between 1 and 4"));
    }

    @Test
    void rejectsMoreThanFourExams() {

        assertTrue(SubjectManagementService.validateSubject(
                "Logic & Algorithms",
                "CS282",
                true,
                40,
                100,
                5
        ).contains("should be between 1 and 4"));
    }
}
