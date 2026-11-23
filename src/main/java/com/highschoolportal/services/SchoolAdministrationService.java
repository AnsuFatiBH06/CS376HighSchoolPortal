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
            return true;
        }
        return false;
    }

    // should be used in future releases to actually send the email
    public String generateEmail(String[] reasons) {
        String draft = "Dear Manager,\nWe are sorry to inform you that your school signup has been rejected.\n"
                        + "Our reasons for the rejection are as follows:\n";
        
        for (int i = 0; i < reasons.length; i++) {
            if (!(reasons[i].isBlank() || reasons[i] == null)) {
                draft += reasons[i] + "\n";
            }
        }

        draft += "Thank you for your understanding, and best of luck moving forward.";
        return draft;
    }
}
