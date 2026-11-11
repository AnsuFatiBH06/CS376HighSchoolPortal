package com.highschoolportal.services;
import com.highschoolportal.database.DynamicSQLQuery;
import com.highschoolportal.model.School;
import javax.swing.JTextField;

public class SchoolRegistrationService {
    public SchoolRegistrationService() {}

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

    public static boolean registerSchool(School school) {
        return DynamicSQLQuery.getInstance().insertSchool(school);
    }
}
