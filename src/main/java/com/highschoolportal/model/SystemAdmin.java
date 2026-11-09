package com.highschoolportal.model;

public class SystemAdmin extends User {
    public SystemAdmin(int userID, String fName, String lName, String email, int credentialsID) {
        super(userID, fName, lName, email, credentialsID);
    }
    
    public void approveSchool(int schoolID) {

    }
    
    public void rejectSchool(int schoolID, String reason1, String reason2, String reason3) {

    }
}
