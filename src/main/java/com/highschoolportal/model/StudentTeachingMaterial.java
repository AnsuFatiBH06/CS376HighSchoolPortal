package com.highschoolportal.model;

import java.time.LocalDateTime;

// UNTESTED
// Stores the information displayed to a student when viewing weekly teaching materials for a subject
public class StudentTeachingMaterial {

    // The teaching material's unique ID
    private final int materialID;

    // The teaching week the material belongs to
    private final int weekNumber;

    // The subject name
    private final String subjectName;

    // The teacher's full name
    private final String teacherName;

    // The location of the PDF file
    private final String filePath;

    // The date and time the material was uploaded
    private final LocalDateTime uploadDate;

    public StudentTeachingMaterial(int materialID, int weekNumber, String subjectName,
                                   String teacherName, String filePath,
                                   LocalDateTime uploadDate) {
        this.materialID = materialID;
        this.weekNumber = weekNumber;
        this.subjectName = subjectName;
        this.teacherName = teacherName;
        this.filePath = filePath;
        this.uploadDate = uploadDate;
    }

    // Getters
    public int getMaterialID() {
        return materialID;
    }

    public int getWeekNumber() {
        return weekNumber;
    }

     public String getSubjectName() {
        return subjectName;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public String getFilePath() {
        return filePath;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }
}
