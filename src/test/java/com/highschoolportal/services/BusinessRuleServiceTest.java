package com.highschoolportal.services;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BusinessRuleServiceTest {

    private final LocalDate start =
            LocalDate.of(2026, 9, 1);

    private final LocalDate end =
            LocalDate.of(2027, 6, 30);

    @Test
    void acceptsValidAcademicYearAndRules() {

        assertNull(BusinessRuleService.validateAcademicYear(
                "2026/2027",
                start,
                end,
                1,
                3,
                5
        ));
    }

    @Test
    void rejectsBlankName() {

        assertTrue(BusinessRuleService.validateAcademicYear(
                " ",
                start,
                end,
                0,
                2,
                5
        ).contains("name"));
    }

    @Test
    void rejectsEndDateBeforeOrEqualToStartDate() {

        assertTrue(BusinessRuleService.validateAcademicYear(
                "2026/2027",
                start,
                start,
                0,
                2,
                5
        ).contains("after"));
    }

    @Test
    void rejectsNegativeOptionalSubjectMinimum() {

        assertTrue(BusinessRuleService.validateAcademicYear(
                "2026/2027",
                start,
                end,
                -1,
                2,
                5
        ).contains("negative"));
    }

    @Test
    void rejectsMinimumOptionalSubjectsGreaterThanMaximum() {

        assertTrue(BusinessRuleService.validateAcademicYear(
                "2026/2027",
                start,
                end,
                3,
                2,
                5
        ).contains("cannot exceed"));
    }

    @Test
    void rejectsRequiredPassedSubjectsBelowOne() {

        assertTrue(BusinessRuleService.validateAcademicYear(
                "2026/2027",
                start,
                end,
                0,
                2,
                0
        ).contains("at least 1"));
    }
}