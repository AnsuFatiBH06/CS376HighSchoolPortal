package com.highschoolportal.services;

import com.highschoolportal.model.School;
import com.highschoolportal.repository.SchoolRepository;

import java.sql.SQLException;
import java.util.List;

public class SchoolAdministrationService {
    private final SchoolRepository repository = new SchoolRepository();

    public List<School> getApprovedSchools() throws SQLException {
        return repository.findApprovedSchools();
    }

    public List<School> getPendingSchools() throws SQLException {
        return repository.findPendingSchools();
    }

    public boolean approveSchool(int schoolID) throws SQLException {
        return repository.approveSchool(schoolID);
    }

    public boolean rejectSchool(int schoolID) throws SQLException {
        boolean rejectionSuccessful = repository.rejectSchool(schoolID);
        if (rejectionSuccessful) {
            // String email = sendEmail(null, null, null)
            return true;
        }
        return false;
    }

    private String sendEmail(String reason1, String reason2, String reason3) {
        String draft = "Dear Manager,\nWe are sorry to inform you that your school signup has been rejected.\n"
                        + "Our reasons for the rejection are as follows:\n";
        
        if (reason1 != null) {
            draft += reason1 + "\n";
        }

        if (reason2 != null) {
            draft += reason2 + "\n";
        }

        if (reason3 != null) {
            draft += reason3 + "\n";
        }

        draft += "Thank you for your understanding, and best of luck moving forward.";
        return draft;
    }
}
