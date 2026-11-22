package com.highschoolportal.model;

import java.time.LocalDateTime;

public class TeachingMaterial {

    // The teaching material's unique ID
    private final int materialID;

    // The ID of the teacher who uploaded the material
    private final int teacherID;

    // The ID of the subject the material belongs to
    private final int subjectAcademicYearID;

    // The teaching week the material belongs to
    private final int weekNumber;

    // The location of the uploaded PDF file
    private final String filePath;

    // The date and time the material was uploaded
    private final LocalDateTime uploadDate;

    public TeachingMaterial(int materialID, int teacherID, int subjectAcademicYearID,
                            int weekNumber, String filePath, LocalDateTime uploadDate) {
        this.materialID = materialID;
        this.teacherID = teacherID;
        this.subjectAcademicYearID = subjectAcademicYearID;
        this.weekNumber = weekNumber;
        this.filePath = filePath;
        this.uploadDate = uploadDate;
    }

    // Getters
    public int getMaterialID() {
        return materialID;
    }

    public int getTeacherID() {
        return teacherID;
    }

    public int getSubjectAcademicYearID() {
        return subjectAcademicYearID;
    }

    public int getWeekNumber() {
        return weekNumber;
    }

    public String getFilePath() {
        return filePath;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }
}
