package com.highschoolportal.model;

import java.time.LocalDate;

public class AcademicYear {
    private final int academicYearID;
    private final int schoolID;
    private final String name;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final int minOptionalSubjects;
    private final int maxOptionalSubjects;
    private final int requiredPassedSubjects;
    private final boolean examPeriodOpen;

    public AcademicYear(int academicYearID, int schoolID, String name,
                        LocalDate startDate, LocalDate endDate,
                        int minOptionalSubjects, int maxOptionalSubjects,
                        int requiredPassedSubjects, boolean examPeriodOpen) {
        this.academicYearID = academicYearID;
        this.schoolID = schoolID;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.minOptionalSubjects = minOptionalSubjects;
        this.maxOptionalSubjects = maxOptionalSubjects;
        this.requiredPassedSubjects = requiredPassedSubjects;
        this.examPeriodOpen = examPeriodOpen;
    }

    public int getAcademicYearID() {
        return academicYearID;
    }

    public int getSchoolID() {
        return schoolID;
    }

    public String getName() {
        return name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public int getMinOptionalSubjects() {
        return minOptionalSubjects;
    }

    public int getMaxOptionalSubjects() {
        return maxOptionalSubjects;
    }

    public int getRequiredPassedSubjects() {
        return requiredPassedSubjects;
    }

    public boolean isExamPeriodOpen() {
        return examPeriodOpen;
    }
}