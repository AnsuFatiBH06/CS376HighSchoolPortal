package com.highschoolportal.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchoolRegistrationServiceTest {

    @Test
    void acceptsValidSchoolDetails() {

        assertNull(SchoolRegistrationService.validateSchoolDetails(
                "Strathclyde School",
                "manager@strath.ac.uk",
                "strath.ac.uk",
                "City of Glasgow, Scotland"
        ));
    }

    @Test
    void rejectsBlankSchoolName() {

        assertTrue(SchoolRegistrationService.validateSchoolDetails(
                " ",
                "manager@strath.ac.uk",
                "strath.ac.uk",
                "City of Glasgow, Scotland"
        ).contains("School Name"));
    }

    @Test
    void rejectsBlankEmail() {

        assertTrue(SchoolRegistrationService.validateSchoolDetails(
                "Strathclyde School",
                " ",
                "strath.ac.uk",
                "City of Glasgow, Scotland"
        ).contains("School Email"));
    }

    @Test
    void rejectsBlankEmailDomain() {

        assertTrue(SchoolRegistrationService.validateSchoolDetails(
                "Strathclyde School",
                "manager@strath.ac.uks",
                " ",
                "City of Glasgow, Scotland"
        ).contains("School Email Domain"));
    }

    @Test
    void rejectsBlankAddress() {

        assertTrue(SchoolRegistrationService.validateSchoolDetails(
                "Strathclyde School",
                "manager@strath.ac.uk",
                "strath.ac.uk",
                " "
        ).contains("School Address"));
    }

    @Test
    void rejectsShortSchoolName() {

        assertTrue(SchoolRegistrationService.validateSchoolDetails(
                "Str",
                "manager@strath.ac.uk",
                "strath.ac.uk",
                "City of Glasgow, Scotland"
        ).contains("between 4 and 35 characters"));
    }

    @Test
    void rejectsLongSchoolName() {

        assertTrue(SchoolRegistrationService.validateSchoolDetails(
                "Strrrrrrrrrrrrrrrrrraaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaathclyyyyyyyyyyyyyyyyyyyyyyyyde",
                "manager@strath.ac.uk",
                "strath.ac.uk",
                "City of Glasgow, Scotland"
        ).contains("between 4 and 35 characters"));
    }

    @Test
    void rejectsInvalidSchoolName() {

        assertTrue(SchoolRegistrationService.validateSchoolDetails(
                "@@@@@@########,,,",
                "manager@strath.ac.uk",
                "strath.ac.uk",
                "City of Glasgow, Scotland"
        ).contains("invalid characters"));
    }

    @Test
    void rejectsInvalidEmail() {

        assertTrue(SchoolRegistrationService.validateSchoolDetails(
                "Strathclyde School",
                "managerstrath.ac.uk",
                "strath.ac.uk",
                "City of Glasgow, Scotland"
        ).contains("valid school email"));
    }

    @Test
    void rejectsInvalidEmailDomain() {

        assertTrue(SchoolRegistrationService.validateSchoolDetails(
                "Strathclyde School",
                "manager@strath.ac.uk",
                "@strathuk",
                "City of Glasgow, Scotland"
        ).contains("valid email domain"));
    }

    @Test
    void rejectsUnmatchingEmailAndDomain() {

        assertTrue(SchoolRegistrationService.validateSchoolDetails(
                "Strathclyde School",
                "manager@test.com",
                "strath.ac.uk",
                "City of Glasgow, Scotland"
        ).contains("Email must match"));
    }

    @Test
    void rejectsShortAddress() {

        assertTrue(SchoolRegistrationService.validateSchoolDetails(
                "Strathclyde School",
                "manager@strath.ac.uk",
                "strath.ac.uk",
                "test"
        ).contains("between 10 and 100 characters"));
    }

    @Test
    void rejectsLongAddress() {

        assertTrue(SchoolRegistrationService.validateSchoolDetails(
                "Strathclyde School",
                "manager@strath.ac.uk",
                "strath.ac.uk",
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        ).contains("between 10 and 100 characters"));
    }

    @Test
    void rejectsInvalidAddress() {

        assertTrue(SchoolRegistrationService.validateSchoolDetails(
                "Strathclyde School",
                "manager@strath.ac.uk",
                "strath.ac.uk",
                "@@@@@@@@:::"
        ).contains("Address contains"));
    }
}