package com.highschoolportal.model;

public class Manager extends User {
    private boolean isSuperManager;
    private int schoolID;

    public Manager(int userID, String fName, String lName, String email, int credentialsID, boolean isSuperManager) {
        super(userID, fName, lName, email, credentialsID);
        this.isSuperManager = isSuperManager;
        this.schoolID = -1;
    } // still doesnt belong to a school

    public Manager(int userID, String fName, String lName, String email, int credentialsID, boolean isSuperManager, int schoolID) {
        super(userID, fName, lName, email, credentialsID);
        this.isSuperManager = isSuperManager;
        this.schoolID = schoolID;
    } // has a school

    public int getSchoolID() {
        return this.schoolID;
    }

    public boolean getIsSuperManager() {
        return this.isSuperManager;
    }

    public void registerSchool() {

    }
}
