
package com.highschoolportal.services;

import com.highschoolportal.model.Manager;
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
        return repository.rejectSchool(schoolID);
    }

    public boolean deleteSchoolAndRetainManager(Manager manager)
            throws SQLException {

        if (manager == null || manager.getUserID() <= 0) {
            throw new IllegalArgumentException(
                    "A valid Manager account is required."
            );
        }

        int schoolID = manager.getSchoolID();

        if (schoolID <= 0) {
            throw new IllegalStateException(
                    "The Manager is not currently assigned to a school."
            );
        }

        boolean deleted = repository.deleteSchoolAndRetainManager(
                schoolID,
                manager.getUserID()
        );

        if (deleted) {
            manager.setSchoolID(-1);
        }

        return deleted;
    }

    public String generateEmail(String[] reasons) {
        String draft = "Dear Manager,\nWe are sorry to inform you that your school signup has been rejected.\n"
                + "Our reasons for the rejection are as follows:\n";

        for (int i = 0; i < reasons.length; i++) {
            if (reasons[i] != null && !reasons[i].isBlank()) {
                draft += reasons[i] + "\n";
            }
        }

        draft += "Thank you for your understanding, and best of luck moving forward.";
        return draft;
    }
}
