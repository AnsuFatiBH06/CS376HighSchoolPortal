package com.highschoolportal.services;

import com.highschoolportal.model.AcademicYear;
import com.highschoolportal.model.Manager;
import com.highschoolportal.repository.AcademicYearRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class BusinessRuleService {

    private final AcademicYearRepository academicYearRepository;

    public BusinessRuleService() {
        this(new AcademicYearRepository());
    }

    BusinessRuleService(AcademicYearRepository academicYearRepository) {
        this.academicYearRepository = academicYearRepository;
    }

    public static String validateAcademicYear(
            String name,
            LocalDate startDate,
            LocalDate endDate,
            int minOptionalSubjects,
            int maxOptionalSubjects,
            int requiredPassedSubjects) {

        if (name == null || name.isBlank()) {
            return "Enter an academic year name, for example 2026/2027.";
        }

        if (name.trim().length() > 50) {
            return "The academic year name cannot exceed 50 characters.";
        }

        if (startDate == null || endDate == null) {
            return "Enter both the start date and end date.";
        }

        if (!startDate.isBefore(endDate)) {
            return "The end date must be after the start date.";
        }

        return validateRules(
                minOptionalSubjects,
                maxOptionalSubjects,
                requiredPassedSubjects
        );
    }

    public static String validateRules(
            int minOptionalSubjects,
            int maxOptionalSubjects,
            int requiredPassedSubjects) {

        if (minOptionalSubjects < 0) {
            return "The minimum optional subjects cannot be negative.";
        }

        if (maxOptionalSubjects < 0) {
            return "The maximum optional subjects cannot be negative.";
        }

        if (minOptionalSubjects > maxOptionalSubjects) {
            return "The minimum optional subjects cannot exceed the maximum.";
        }

        if (requiredPassedSubjects < 1) {
            return "The required passed subjects must be at least 1.";
        }

        return null;
    }

    public AcademicYear createAcademicYear(
            Manager manager,
            String name,
            LocalDate startDate,
            LocalDate endDate,
            int minOptionalSubjects,
            int maxOptionalSubjects,
            int requiredPassedSubjects) throws SQLException {

        validateManager(manager);

        String validationError = validateAcademicYear(
                name,
                startDate,
                endDate,
                minOptionalSubjects,
                maxOptionalSubjects,
                requiredPassedSubjects
        );

        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }

        if (!academicYearRepository.isSchoolApproved(manager.getSchoolID())) {
            throw new IllegalStateException(
                    "Only managers of an approved school can create academic years."
            );
        }

        String cleanName = name.trim();

        if (academicYearRepository.existsBySchoolAndName(
                manager.getSchoolID(), cleanName)) {

            throw new IllegalArgumentException(
                    "An academic year with this name already exists for your school."
            );
        }

        AcademicYear academicYear = new AcademicYear(
                0,
                manager.getSchoolID(),
                cleanName,
                startDate,
                endDate,
                minOptionalSubjects,
                maxOptionalSubjects,
                requiredPassedSubjects,
                false
        );

        return academicYearRepository.create(academicYear);
    }

    public List<AcademicYear> getAcademicYears(Manager manager)
            throws SQLException {

        validateManager(manager);

        return academicYearRepository.findBySchoolID(manager.getSchoolID());
    }

    public void updateRules(
            Manager manager,
            int academicYearID,
            int minOptionalSubjects,
            int maxOptionalSubjects,
            int requiredPassedSubjects) throws SQLException {

        validateManager(manager);

        String validationError = validateRules(
                minOptionalSubjects,
                maxOptionalSubjects,
                requiredPassedSubjects
        );

        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }

        if (academicYearID <= 0) {
            throw new IllegalArgumentException(
                    "Select an academic year first."
            );
        }

        if (academicYearRepository.findByIDAndSchoolID(
                academicYearID, manager.getSchoolID()).isEmpty()) {

            throw new IllegalArgumentException(
                    "The selected academic year does not belong to your school."
            );
        }

        if (!academicYearRepository.isSchoolApproved(manager.getSchoolID())) {
            throw new IllegalStateException(
                    "Only managers of an approved school can change academic-year rules."
            );
        }

        int availableOptionalSubjects =
                academicYearRepository.countOptionalSubjects(academicYearID);

        if (availableOptionalSubjects > 0
                && maxOptionalSubjects > availableOptionalSubjects) {

            throw new IllegalArgumentException(
                    "The maximum optional subjects cannot exceed the number of "
                            + "optional subjects offered in this academic year ("
                            + availableOptionalSubjects + ")."
            );
        }

        int availableSubjects =
                academicYearRepository.countSubjects(academicYearID);

        if (availableSubjects > 0
                && requiredPassedSubjects > availableSubjects) {

            throw new IllegalArgumentException(
                    "Required passed subjects cannot exceed the number of "
                            + "subjects offered in this academic year ("
                            + availableSubjects + ")."
            );
        }

        if (!academicYearRepository.updateRules(
                academicYearID,
                manager.getSchoolID(),
                minOptionalSubjects,
                maxOptionalSubjects,
                requiredPassedSubjects)) {

            throw new SQLException(
                    "The academic-year rules could not be updated."
            );
        }
    }

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