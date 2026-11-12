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
        return repository.rejectSchool(schoolID);
    }
}
