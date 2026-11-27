
package com.highschoolportal.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import com.highschoolportal.database.MySQLConnect;
import com.highschoolportal.model.Credentials;
import com.highschoolportal.model.Manager;
import com.highschoolportal.model.Student;
import com.highschoolportal.model.SystemAdmin;
import com.highschoolportal.model.User;
import com.highschoolportal.model.Manager.SuperManagerStatus;

public class UserRepository {

    public Optional<User> findByCredentialsId(
            int credentialsId,
            Credentials.Role role
    ) throws SQLException {

        if (role == null || credentialsId <= 0) {
            return Optional.empty();
        }

        String sql = switch (role) {
            case Admin -> """
                SELECT AdminID AS UserID,
                       FName, LName, Email, CredentialsID
                FROM SystemAdmin
                WHERE CredentialsID = ?
                """;

            case Manager -> """
                SELECT ManagerID AS UserID,
                       FName, LName, Email,
                       CredentialsID, SchoolID, SuperManagerStatus
                FROM Manager
                WHERE CredentialsID = ?
                """;

            case Teacher -> """
                SELECT TeacherID AS UserID,
                       FName, LName, Email, CredentialsID
                FROM Teacher
                WHERE CredentialsID = ?
                """;

            case Student -> """
                SELECT StudentID AS UserID,
                       FName, LName, Email, CredentialsID
                FROM Student
                WHERE CredentialsID = ?
                """;
        };

        try (Connection connection =
                     MySQLConnect.getInstance().getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, credentialsId);

            try (ResultSet result = statement.executeQuery()) {

                if (!result.next()) {
                    return Optional.empty();
                }

                int userId = result.getInt("UserID");
                String firstName = result.getString("FName");
                String lastName = result.getString("LName");
                String email = result.getString("Email");
                int accountId = result.getInt("CredentialsID");

                User user;

                switch (role) {
                    case Admin ->
                        user = new SystemAdmin(
                            userId, firstName, lastName,
                            email, accountId
                        );

                    case Manager -> {
                        Integer schoolId = (Integer)
                            result.getObject("SchoolID");
                        
                        String superManagerStatus =
                            result.getString("SuperManagerStatus");

                        if (schoolId == null) {
                            user = new Manager(
                                userId, firstName, lastName,
                                email, accountId, SuperManagerStatus.valueOf(superManagerStatus.toUpperCase())
                            );
                        } else {
                            user = new Manager(
                                userId, firstName, lastName,
                                email, accountId, SuperManagerStatus.valueOf(superManagerStatus.toUpperCase()),
                                schoolId
                            );
                        }
                    }

                    case Teacher -> 
                        user = new User(
                            userId, firstName, lastName,
                            email, accountId
                        );

                    case Student -> {
                        Integer schoolId = (Integer)
                            result.getObject("SchoolID");
                            if (schoolId == null) {
                                user = new Student(
                                    userId, firstName, lastName,
                                    email, accountId
                                );
                            } else {
                                user = new Student(
                                    userId, firstName, lastName,
                                    email, accountId, schoolId);
                            }
                    }

                    default ->
                        throw new SQLException("Unsupported role");
                }

                if (result.next()) {
                    throw new SQLException(
                        "Duplicate user profile for credentials ID "
                        + credentialsId
                    );
                }

                return Optional.of(user);
            }
        }
    }
}
