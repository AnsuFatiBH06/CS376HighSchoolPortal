package com.highschoolportal.model;

import java.time.LocalDateTime;

public class PasswordResetRequest {
    private final int requestID;
    private final int credentialsID;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String role;
    private final String status;
    private final LocalDateTime requestedAt;

    public PasswordResetRequest(int requestID, int credentialsID, String firstName,
            String lastName, String email, String role, String status,
            LocalDateTime requestedAt) {
        this.requestID = requestID;
        this.credentialsID = credentialsID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.status = status;
        this.requestedAt = requestedAt;
    }

    public int getRequestID() { return requestID; }
    public int getCredentialsID() { return credentialsID; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
}
