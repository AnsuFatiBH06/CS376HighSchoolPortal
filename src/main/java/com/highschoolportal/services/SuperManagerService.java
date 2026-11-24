package com.highschoolportal.services;

import com.highschoolportal.model.Manager;
import com.highschoolportal.repository.ManagerRepository;

import java.sql.SQLException;
import java.util.List;


public class SuperManagerService {
    private final ManagerRepository managerRepository;

    public SuperManagerService() {
        this(new ManagerRepository());
    }

    SuperManagerService(ManagerRepository managerRepository) {
        this.managerRepository = managerRepository;
    }

    public boolean requestSuperStatus(Manager manager) {
        try {
            return this.managerRepository.requestSuperManagerStatus(manager);
        } catch (SQLException exception) {
            return false;
        }
    }

    public List<Manager> getApprovedSuperManagers() throws SQLException {
        return this.managerRepository.findApprovedSuperManagers();
    }

    public List<Manager> getPendingSuperManagers() throws SQLException {
        return this.managerRepository.findPendingSuperManagers();
    }

    public boolean approveSuperManager(int managerID) throws SQLException {
        return this.managerRepository.approveSuperManager(managerID);
    }

    public boolean rejectSuperManager(int managerID) throws SQLException {
        return this.managerRepository.rejectSuperManager(managerID);
    }
}
