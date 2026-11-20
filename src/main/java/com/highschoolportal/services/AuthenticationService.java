
package com.highschoolportal.services;

import java.sql.SQLException;
import java.util.Optional;

import com.highschoolportal.model.Credentials;
import com.highschoolportal.repository.CredentialsRepository;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class AuthenticationService {

    private final CredentialsRepository repository;

    public AuthenticationService() {
        this(new CredentialsRepository());
    }

    public AuthenticationService(CredentialsRepository repository) {
        this.repository = repository;
    }

    public record LoginResult(
        int credentialsID,
        Credentials.Role role,
        boolean mustChangePassword
    ) {}

    public Optional<LoginResult> login(
            String email,
            char[] password
    ) throws SQLException {

        if (email == null || email.isBlank()
                || password == null || password.length == 0) {
            return Optional.empty();
        }

        Optional<Credentials> account =
                repository.findByEmail(email.trim());

        if (account.isEmpty()) {
            return Optional.empty();
        }

        Credentials credentials = account.get();

        if (!credentials.isActive()) {
            return Optional.empty();
        }

        boolean passwordMatches;

        try {
            passwordMatches = BCrypt.verifyer()
                    .verify(password, credentials.getPasswordHash())
                    .verified;
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }

        if (!passwordMatches) {
            return Optional.empty();
        }

        return Optional.of(new LoginResult(
                credentials.getId(),
                credentials.getRole(),
                credentials.mustChangePassword()
        ));
    }
}
