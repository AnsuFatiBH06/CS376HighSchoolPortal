package com.highschoolportal.model;

public class Manager extends User {
    public enum SuperManagerStatus {
        NONE,
        PENDING,
        APPROVED
    }

    private SuperManagerStatus superManagerStatus;
    private int schoolID;

    public Manager(int userID, String fName, String lName, String email, int credentialsID, SuperManagerStatus superManagerStatus) {
        super(userID, fName, lName, email, credentialsID);
        this.superManagerStatus = superManagerStatus;
        this.schoolID = -1;
    } // still doesnt belong to a school

    public Manager(int userID, String fName, String lName, String email, int credentialsID, SuperManagerStatus superManagerStatus, int schoolID) {
        super(userID, fName, lName, email, credentialsID);
        this.superManagerStatus = superManagerStatus;
        this.schoolID = schoolID;
    } // has a school

    public int getSchoolID() {
        return this.schoolID;
    }

    public SuperManagerStatus getSuperManagerStatus() {
        return this.superManagerStatus;
    }

    public void setSuperManagerStatus(SuperManagerStatus newStatus) {
        this.superManagerStatus = newStatus;
    }
}
