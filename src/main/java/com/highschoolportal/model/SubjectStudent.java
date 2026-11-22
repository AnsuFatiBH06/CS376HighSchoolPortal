package com.highschoolportal.model;

// Stores information displayed to a teacher when viewing students enrolled in one of their subjects
public class SubjectStudent {

    // The student's unique ID
    private final int studentID;

    // The student's full name
    private final String studentName;

    // The student's school email
    private final String email;

    public SubjectStudent(int studentID, String studentName, String email) {
        this.studentID = studentID;
        this.studentName = studentName;
        this.email = email;
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
}
