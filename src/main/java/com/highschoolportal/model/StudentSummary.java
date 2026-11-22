package com.highschoolportal.model;

// Stores the information displayed when viewing or filtering students 
public class StudentSummary {

     // The student's unique ID
    private final int studentID;

    // The student's full name
    private final String studentName;

    // The student's school email
    private final String email;

    // The academic year the student is enrolled in.
    private final String academicYearName;

    // The student's current certificate qualification status
    private final String certificateStatus;

    public StudentSummary(int studentID, String studentName, String email,
                          String academicYearName, String certificateStatus) {
        this.studentID = studentID;
        this.studentName = studentName;
        this.email = email;
        this.academicYearName = academicYearName;
        this.certificateStatus = certificateStatus;
    }

    // Getters
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

    public String getCertificateStatus() {
        return certificateStatus;
    }
}
