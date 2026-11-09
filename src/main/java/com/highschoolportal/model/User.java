package com.highschoolportal.model;

public class User {
    private int userID;
    private String fName;
    private String lName;
    private String email;
    private int credentialsID;

    public User(int userID, String fName, String lName, String email, int credentialsID) {
        this.userID = userID;
        this.fName = fName;
        this.lName = lName;
        this.email = email;
        this.credentialsID = credentialsID;
    }

    public int getUserID() {
        return this.userID;
    }

    public String getFirstName() {
        return this.fName;
    }

    public String getLastName() {
        return this.lName;
    }

    public String getEmail() {
        return this.email;
    }

    public int getCredentialsID() {
        return this.credentialsID;
    }
}
