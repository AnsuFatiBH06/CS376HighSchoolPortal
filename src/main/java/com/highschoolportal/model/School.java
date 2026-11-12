package com.highschoolportal.model;

public class School {
    private int schoolID;
    private String schoolName;
    private String schoolEmail;
    private String schoolEmailDomain;
    private String schoolAddress;
    private String schoolStatus;

    public School(String schoolName, String schoolEmail, String schoolEmailDomain, String schoolAddress, String schoolStatus) {
        this.schoolName = schoolName;
        this.schoolEmail = schoolEmail;
        this.schoolEmailDomain = schoolEmailDomain;
        this.schoolAddress = schoolAddress;
        this.schoolStatus = schoolStatus;
    }

    public School(int schoolID, String schoolName, String schoolEmail, String schoolEmailDomain, String schoolAddress, String schoolStatus) {
        this.schoolID = schoolID;
        this.schoolEmail = schoolEmail;
        this.schoolEmailDomain = schoolEmailDomain;
        this.schoolAddress = schoolAddress;
        this.schoolStatus = schoolStatus;
    }

    public void updateStatus(String newStatus) {
        this.schoolStatus = newStatus;
    }

    public int getSchoolID() {
        return this.schoolID;
    }

    public String getSchoolName() {
        return this.schoolName;
    }

    public String getSchoolEmail() {
        return this.schoolEmail;
    }

    public String getSchoolEmailDomain() {
        return this.schoolEmailDomain;
    }

    public String getSchoolAddress() {
        return this.schoolAddress;
    }

    public String getSchoolStatus() {
        return this.schoolStatus;
    }
}
