package com.highschoolportal.services;

import java.sql.SQLException;
import java.util.Optional;

import com.highschoolportal.model.Credentials;
import com.highschoolportal.model.User;
import com.highschoolportal.repository.CredentialsRepository;
import com.highschoolportal.repository.UserRepository;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class AuthenticationService {

    private final CredentialsRepository credentialsRepository;
    private final UserRepository userRepository;

    public record LoginResult(
            User user,
            Credentials.Role role,
            boolean mustChangePassword
            ) {}

    public AuthenticationService() {
        this(
                new CredentialsRepository(),
                new UserRepository()
        );
    }

    public AuthenticationService(
            CredentialsRepository credentialsRepository,
            UserRepository userRepository) {

        this.credentialsRepository = credentialsRepository;
        this.userRepository = userRepository;
    }

    public Optional<LoginResult> login(
            String email,
            char[] password
    ) throws SQLException {

        if (email == null || email.isBlank()
                || password == null || password.length == 0) {
            return Optional.empty();
        }

        Optional<Credentials> account
                = credentialsRepository.findByEmail(email.trim());

        if (account.isEmpty()) {
            return Optional.empty();
        }

        Credentials credentials = account.get();

        if (!credentials.isActive()) {
            return Optional.empty();
        }

        boolean matches;

        try {
            matches = BCrypt.verifyer()
                    .verify(password, credentials.getPasswordHash()).verified;
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }

        if (!matches) {
            return Optional.empty();
        }

        Optional<User> user = userRepository.findByCredentialsId(
                credentials.getId(),
                credentials.getRole()
        );

        if (user.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new LoginResult(
                user.get(),
                credentials.getRole(),
                credentials.mustChangePassword()
        ));
    }

    public Optional<LoginResult> restoreLogin(int credentialsID)
            throws SQLException {

        Optional<Credentials> account
                = credentialsRepository.findById(credentialsID);

        if (account.isEmpty()) {
            return Optional.empty();
        }

        Credentials credentials = account.get();

        if (!credentials.isActive()
                || credentials.mustChangePassword()) {
            return Optional.empty();
        }

        Optional<User> user
                = userRepository.findByCredentialsId(
                        credentials.getId(),
                        credentials.getRole()
                );

        return user.map(value -> new LoginResult(
                value,
                credentials.getRole(),
                false
        ));
    }

}
