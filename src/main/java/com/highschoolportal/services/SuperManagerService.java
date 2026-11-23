package com.highschoolportal.services;

import com.highschoolportal.model.Manager;
import com.highschoolportal.repository.ManagerRepository;

import java.sql.SQLException;


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
}
