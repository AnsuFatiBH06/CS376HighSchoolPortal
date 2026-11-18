package com.highschoolportal.services;

import com.highschoolportal.model.Subject;
import com.highschoolportal.model.Manager;
import com.highschoolportal.repository.SubjectRepository;

import java.sql.SQLException;
import java.util.List;

public class SubjectManagementService {

    private final SubjectRepository subjectRepository;

    public SubjectManagementService() {
        this(new SubjectRepository());
    }

    SubjectManagementService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public static String validateSubject(
            String name,
            String code,
            boolean isCore,
            float passMark,
            float maxMark,
            int examSeriesNumber) {

        if (name == null || name.isBlank()) {
            return "Subject Name should not be empty.";
        }

        if (code == null || code.isBlank()) {
            return "Subject Code should not be empty.";
        }

        if (name.trim().length() < 3 || name.trim().length() > 45) {
            return "Subject Name should be between 3 and 45 characters.";
        }

        if (!name.matches("^[A-Za-z0-9 '&-]+$")) {
            return "Enter a valid subject name.";
        }

        if (code.trim().length() < 3 || code.trim().length() > 45) {
            return "Subject Code should be between 3 and 15 characters.";
        }

        if(!code.matches("^[A-Z0-9-]+$")) {
            return "Subject Code can only include uppercase letters, numbers, and hyphens";
        }

        return validateRules(
                passMark,
                maxMark,
                examSeriesNumber
        );
    }

    public static String validateRules(
            float passMark,
            float maxMark,
            int examSeriesNumber) {

        if (passMark < 0.0) {
            return "The pass mark cannot be negative.";
        }

        if (maxMark < 0.0) {
            return "The maximum mark cannot be negative.";
        }

        if (passMark > maxMark) {
            return "The pass mark cannot exceed the maximum mark.";
        }

        if (examSeriesNumber < 1 || examSeriesNumber > 4) {
            return "Exam Series Number should be between 1 and 4.";
        }

        return null;
    }

    public Subject createSubject(
            Manager manager,
            String name,
            String code,
            boolean isCore,
            float passMark,
            float maxMark,
            int examSeriesNumber) throws SQLException {

        validateManager(manager);

        String validationError = validateSubject(
                name,
                code,
                isCore,
                passMark,
                maxMark,
                examSeriesNumber
        );

        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }

        if (!subjectRepository.isSchoolApproved(manager.getSchoolID())) {
            throw new IllegalStateException(
                    "Only managers of an approved school can create subjects."
            );
        }

        String cleanName = name.trim();

        if (subjectRepository.existsBySchoolAndName(
                manager.getSchoolID(), cleanName)) {

            throw new IllegalArgumentException(
                    "A subject with this name already exists for your school."
            );
        }

        Subject subject = new Subject(
                0,
                name,
                code,
                isCore,
                passMark,
                maxMark,
                examSeriesNumber,
                manager.getSchoolID()
        );

        return subjectRepository.create(subject);
    }

    public List<Subject> getSubjects(Manager manager) throws SQLException {
        validateManager(manager);
        return subjectRepository.findBySchoolID(manager.getSchoolID());
    }

    // public void updateRules(
    //         Manager manager,
    //         int academicYearID,
    //         int minOptionalSubjects,
    //         int maxOptionalSubjects,
    //         int requiredPassedSubjects) throws SQLException {

    //     validateManager(manager);

    //     String validationError = validateRules(
    //             minOptionalSubjects,
    //             maxOptionalSubjects,
    //             requiredPassedSubjects
    //     );

    //     if (validationError != null) {
    //         throw new IllegalArgumentException(validationError);
    //     }

    //     if (academicYearID <= 0) {
    //         throw new IllegalArgumentException(
    //                 "Select an academic year first."
    //         );
    //     }

    //     if (academicYearRepository.findByIDAndSchoolID(
    //             academicYearID, manager.getSchoolID()).isEmpty()) {

    //         throw new IllegalArgumentException(
    //                 "The selected academic year does not belong to your school."
    //         );
    //     }

    //     if (!academicYearRepository.isSchoolApproved(manager.getSchoolID())) {
    //         throw new IllegalStateException(
    //                 "Only managers of an approved school can change academic-year rules."
    //         );
    //     }

    //     int availableOptionalSubjects =
    //             academicYearRepository.countOptionalSubjects(academicYearID);

    //     if (availableOptionalSubjects > 0
    //             && maxOptionalSubjects > availableOptionalSubjects) {

    //         throw new IllegalArgumentException(
    //                 "The maximum optional subjects cannot exceed the number of "
    //                         + "optional subjects offered in this academic year ("
    //                         + availableOptionalSubjects + ")."
    //         );
    //     }

    //     int availableSubjects =
    //             academicYearRepository.countSubjects(academicYearID);

    //     if (availableSubjects > 0
    //             && requiredPassedSubjects > availableSubjects) {

    //         throw new IllegalArgumentException(
    //                 "Required passed subjects cannot exceed the number of "
    //                         + "subjects offered in this academic year ("
    //                         + availableSubjects + ")."
    //         );
    //     }

    //     if (!academicYearRepository.updateRules(
    //             academicYearID,
    //             manager.getSchoolID(),
    //             minOptionalSubjects,
    //             maxOptionalSubjects,
    //             requiredPassedSubjects)) {

    //         throw new SQLException(
    //                 "The academic-year rules could not be updated."
    //         );
    //     }
    // }

    private void validateManager(Manager manager) {
        if (manager == null) {
            throw new IllegalStateException(
                    "No manager is currently signed in."
            );
        }

        if (manager.getSchoolID() <= 0) {
            throw new IllegalStateException(
                    "Your manager account is not associated with a school."
            );
        }
    }
}