package com.highschoolportal.services;

import java.util.List;

import com.highschoolportal.model.Subject;
import com.highschoolportal.repository.StudentCurriculumRepository;

public class StudentCurriculumService {
    private final StudentCurriculumRepository repository;
    public StudentCurriculumService() {
        this.repository = new StudentCurriculumRepository();
    }

    public List<Subject> getAvailableOptionals(int studentID) {
        return this.repository.getAvailableOptionals(studentID);
    }

    public List<Subject> getCoreSubjects(int studentID) {
        return this.repository.getCoreSubjects(studentID);
    }

    public List<Subject> getSelectedSubjects(int studentID) {
        return this.repository.getSelectedSubjects(studentID);
    }
}
