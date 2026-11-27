package com.highschoolportal.model;

public class Student extends User {
    private int schoolID;

    public Student(int userID, String fName, String lName, String email, int credentialsID, int schoolID) {
        super(userID, fName, lName, email, credentialsID);
        this.schoolID = schoolID;
    }

    public Student(int userID, String fName, String lName, String email, int credentialsID) {
        super(userID, fName, lName, email, credentialsID);
        this.schoolID = -1;
    }

    public int getSchoolID() {
        return this.schoolID;
    }
}
