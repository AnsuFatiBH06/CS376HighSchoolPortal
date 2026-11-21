
package com.highschoolportal.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HexFormat;
import java.util.OptionalInt;

import com.highschoolportal.repository.RememberMeTokenRepository;

public class RememberMeService {

    private final RememberMeTokenRepository repository;
    private final SecureRandom secureRandom = new SecureRandom();

    public RememberMeService() {
        this(new RememberMeTokenRepository());
    }

    public RememberMeService(RememberMeTokenRepository repository) {
        this.repository = repository;
    }

    public String createToken(int credentialsID) throws SQLException {

        if (credentialsID <= 0) {
            throw new IllegalArgumentException("Invalid credentials ID");
        }

        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        repository.save(credentialsID, hashToken(token));

        return token;
    }

    public OptionalInt validateToken(String token)
            throws SQLException {

        if (token == null || token.isBlank()) {
            return OptionalInt.empty();
        }

        return repository.findValidCredentialsID(
                hashToken(token)
        );
    }

    public void revokeToken(String token) throws SQLException {

        if (token == null || token.isBlank()) {
            return;
        }

        repository.revoke(hashToken(token));
    }

    private String hashToken(String token) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 is unavailable", e
            );
        }
    }
}
