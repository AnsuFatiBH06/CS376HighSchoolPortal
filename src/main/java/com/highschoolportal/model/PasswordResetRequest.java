package com.highschoolportal.model;

import java.time.LocalDateTime;

public class PasswordResetRequest {

    private int requestId;
    private int credentialsId;
    private String newPasswordHash;
    private LocalDateTime requestedAt;
    private String status;
    private Integer reviewedByCredentialsId;
    private LocalDateTime reviewedAt;

public PasswordResetRequest(int requestId, int credentialsId,
    String newPasswordHash, LocalDateTime requestedAt,
    String status, Integer reviewedByCredentialsId,
    LocalDateTime reviewedAt) {
        this.requestId = requestId;
        this.credentialsId = credentialsId;
        this.newPasswordHash = newPasswordHash;
        this.requestedAt = requestedAt;
        this.status = status;
        this.reviewedByCredentialsId = reviewedByCredentialsId;
        this.reviewedAt = reviewedAt;
    }

    public int getRequestId() {
        return requestId;
    }

    public int getCredentialsId() {
        return credentialsId;
    }

    public String getNewPasswordHash() {
        return newPasswordHash;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public String getStatus() {
        return status;
    }

    public Integer getReviewedByCredentialsId() {
        return reviewedByCredentialsId;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }
}
