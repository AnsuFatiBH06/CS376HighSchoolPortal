package com.highschoolportal.services;
import com.highschoolportal.model.School;
import com.highschoolportal.repository.SchoolRepository;

import java.sql.SQLException;

public class SchoolRegistrationService {
    private final SchoolRepository schoolRepository;

    public SchoolRegistrationService() {
        this(new SchoolRepository());
    }

    SchoolRegistrationService(SchoolRepository schoolRepository) {
        this.schoolRepository = schoolRepository;
    }

    public static String validateSchoolDetails(String schoolName, String email, String emailDomain, String address) {

        if (schoolName == null || schoolName.isBlank()) {
            return "School Name should not be empty";
        }

        if (email == null || email.isBlank()) {
            return "School Email should not be empty";
        }

        if (emailDomain == null || emailDomain.isBlank()) {
            return "School Email Domain should not be empty";
        }

        if (address == null || address.isBlank()) {
            return "School Address should not be empty";
        }

        if (schoolName.length() < 4 || schoolName.length() > 35) {
            return "School Name should have more than 4 letters and less than 35 letters";
        }

        if(!schoolName.matches("^[A-Za-z0-9 .'-]+$")) {
            return "School name contains invalid characters.";
        }   

        if (!email.contains("@")) {
            //display error next to field
            return "School Email should contain the character: '@'";
        }

        if(!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return "Enter a valid school email.";
        }

        if (!emailDomain.contains(".")) {
            //display error next to field
            return "Email Domain should contain '.' at least once.";
        }

        if(!emailDomain.matches("^[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return "Enter a valid email domain.";
        }

        String emailDomainPart = email.substring(email.indexOf("@") + 1);

        if(!emailDomainPart.equalsIgnoreCase(emailDomain)) {
            return "Email must match the school domain.";
        }

        if (address.length() < 10 || address.length() > 100) {
            return "Address should have more than 10 characters and less than 100 characters";
        }

        if(!address.matches("^[A-Za-z0-9 ,./#-]+$")) {
            return "Address contains invalid characters.";
        }

        return null;
    }

    public boolean registerSchool(School school) {
        try {
            return this.schoolRepository.insertPendingSchool(school);
        } catch (SQLException e) {
            return false;
        }
    }
}
