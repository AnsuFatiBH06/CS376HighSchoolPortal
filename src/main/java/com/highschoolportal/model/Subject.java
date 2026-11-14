package com.highschoolportal.model;

public class Subject {
    private int subjectID;
    private String subjectName;
    private String subjectCode;
    private boolean isCore;
    private float passMark;
    private float maxMark;
    private int examSeriesNumber;
    private int schoolID;

    public Subject(int subjectID, String subjectName, String subjectCode, boolean isCore,
                   float passMark, float maxMark, int examSeriesNumber, int schoolID) {
        this.subjectID = subjectID;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.isCore = isCore;
        this.passMark = passMark;
        this.maxMark = maxMark;
        this.examSeriesNumber = examSeriesNumber;
        this.schoolID = schoolID;
    }

    public Subject(String subjectName, String subjectCode, boolean isCore,
                   float passMark, float maxMark, int examSeriesNumber, int schoolID) {
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.isCore = isCore;
        this.passMark = passMark;
        this.maxMark = maxMark;
        this.examSeriesNumber = examSeriesNumber;
        this.schoolID = schoolID;
    }

    public int getSubjectID() {
        return this.subjectID;
    }

    public String getSubjectName() {
        return this.subjectName;
    }

    public String getSubjectCode() {
        return this.subjectCode;
    }

    public boolean getIsCore() {
        return this.isCore;
    }

    public float getPassMark() {
        return this.passMark;
    }

    public float getMaxMark() {
        return this.maxMark;
    }

    public int getExamSeriesNumber() {
        return this.examSeriesNumber;
    }

    public int getSchoolID() {
        return this.schoolID;
    }
}
