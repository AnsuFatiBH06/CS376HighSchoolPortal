package com.highschoolportal.services;
import com.highschoolportal.model.School;
import com.highschoolportal.repository.SchoolRepository;

import java.sql.SQLException;

import javax.swing.JTextField;

public class SchoolRegistrationService {
    private final SchoolRepository schoolRepository;

    public SchoolRegistrationService() {
        this(new SchoolRepository());
    }

    SchoolRegistrationService(SchoolRepository schoolRepository) {
        this.schoolRepository = schoolRepository;
    }

    public static boolean validateSchoolDetails(
                                JTextField schoolNameField, JTextField emailField,
                                JTextField emailDomainField, JTextField addressField
                            ) {
        String schoolName = schoolNameField.getText();
        String email = emailField.getText();
        String emailDomain = emailDomainField.getText();
        String address = addressField.getText();

        if (
            schoolName.equals("")
            || email.equals("")
            || emailDomain.equals("")
            || address.equals("")
        ) {
            return false;
        }

        if (!email.contains("@")) {
            //display error next to field
            return false;
        }

        if (!emailDomain.contains(".")) {
            //display error next to field
            return false;
        }

        return true;
    }

    public boolean registerSchool(School school) {
        try {
            return this.schoolRepository.insertPendingSchool(school);
        } catch (SQLException e) {
            return false;
        }
    }
}
