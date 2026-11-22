package com.highschoolportal.model;

public class IncompleteCurriculumStudent {

    // The student's unique ID
    private final int studentID;

    // The student's full name
    private final String studentName;

    // The student's school email 
    private final String email;

    // The academic year in which the student is enrolled
    private final String academicYearName;

    // The number of optional subjects the student has selected
    private final int optionalSubjectsSelected;

    // The minimum number of optional subjects required by the academic year
    private final int minimumOptionalSubjects;

    public IncompleteCurriculumStudent(int studentID, String studentName, String email,
                                       String academicYearName, int optionalSubjectsSelected,
                                       int minimumOptionalSubjects) {
        this.studentID = studentID;
        this.studentName = studentName;
        this.email = email;
        this.academicYearName = academicYearName;
        this.optionalSubjectsSelected = optionalSubjectsSelected;
        this.minimumOptionalSubjects = minimumOptionalSubjects;
    }

    public int getStudentID() {
        return studentID;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getEmail() {
        return email;
    }

    public String getAcademicYearName() {
        return academicYearName;
    }

    public int getOptionalSubjectsSelected() {
        return optionalSubjectsSelected;
    }

    public int getMinimumOptionalSubjects() {
        return minimumOptionalSubjects;
    }
}
