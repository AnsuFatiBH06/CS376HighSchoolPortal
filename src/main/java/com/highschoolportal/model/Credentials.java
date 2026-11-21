
package com.highschoolportal.model;

/**
 * Represents a user's authentication credentials.
 * Passwords are stored as hashes.
 */
public class Credentials {

    public enum Role {
        Admin, Manager, Teacher, Student
    }

    public enum Status {
        Active, Inactive
    }

    private final int id;
    private final Role role;
    private final String passwordHash;
    private final Status status;
    private final boolean mustChangePassword;

    public Credentials(int id, Role role, String passwordHash,
                       Status status, boolean mustChangePassword) {
        this.id = id;
        this.role = role;
        this.passwordHash = passwordHash;
        this.status = status;
        this.mustChangePassword = mustChangePassword;
    }

    public int getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Status getStatus() {
        return status;
    }

    public boolean mustChangePassword() {
        return mustChangePassword;
    }

    public boolean isActive() {
        return status == Status.Active;
    }
}
