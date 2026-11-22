package com.highschoolportal.model;

public class StudentEnrollmentRequest {
    
    // Student's Information
    private final String firstName;
    private final String lastName;
    private final String dateOfBirth;
    private final int academicYearID;

    // Creates a new student enrollment request
    public StudentEnrollmentRequest(String firstName, String lastName, String dateOfBirth, int academicYearID) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.academicYearID = academicYearID;
    }

    // Getters 
    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public int getAcademicYearID() {
        return academicYearID;
    }
}
