package com.highschoolportal.model;

public class Manager extends User {
    private String superManagerStatus;
    private int schoolID;

    public Manager(int userID, String fName, String lName, String email, int credentialsID, String superManagerStatus) {
        super(userID, fName, lName, email, credentialsID);
        this.superManagerStatus = superManagerStatus;
        this.schoolID = -1;
    } // still doesnt belong to a school

    public Manager(int userID, String fName, String lName, String email, int credentialsID, String superManagerStatus, int schoolID) {
        super(userID, fName, lName, email, credentialsID);
        this.superManagerStatus = superManagerStatus;
        this.schoolID = schoolID;
    } // has a school

    public int getSchoolID() {
        return this.schoolID;
    }

    public String getSuperManagerStatus() {
        return this.superManagerStatus;
    }
}
