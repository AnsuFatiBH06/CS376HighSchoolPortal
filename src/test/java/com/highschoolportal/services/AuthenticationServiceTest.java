
package com.highschoolportal.services;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.highschoolportal.model.Credentials;
import com.highschoolportal.model.User;
import com.highschoolportal.repository.CredentialsRepository;
import com.highschoolportal.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationServiceTest {

    private FakeCredentialsRepository credentialsRepo;
    private FakeUserRepository userRepo;
    private AuthenticationService service;

    private final String email = "test@example.com";
    private final String password = "TestPassword123!";

    @BeforeEach
    void setup() {
        credentialsRepo = new FakeCredentialsRepository();
        userRepo = new FakeUserRepository();

        service = new AuthenticationService(
                credentialsRepo,
                userRepo
        );

        userRepo.user = Optional.of(
                new User(1, "Test", "User", email, 1)
        );
    }

    private Credentials account(
            Credentials.Status status,
            boolean mustChangePassword) {

        String hash = BCrypt.withDefaults()
                .hashToString(4, password.toCharArray());

        return new Credentials(
                1,
                Credentials.Role.Admin,
                hash,
                status,
                mustChangePassword
        );
    }

    @Test
    void correctPasswordAllowsLogin() throws SQLException {
        credentialsRepo.account = Optional.of(
                account(Credentials.Status.Active, false)
        );

        var result = service.login(
                email, password.toCharArray()
        );

        assertTrue(result.isPresent());
        assertEquals(Credentials.Role.Admin, result.get().role());
        assertEquals(1, result.get().user().getCredentialsID());
    }

    @Test
    void incorrectPasswordIsRejected() throws SQLException {
        credentialsRepo.account = Optional.of(
                account(Credentials.Status.Active, false)
        );

        var result = service.login(
                email, "WrongPassword!".toCharArray()
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void unknownEmailIsRejected() throws SQLException {
        credentialsRepo.account = Optional.empty();

        assertTrue(
                service.login(email, password.toCharArray()).isEmpty()
        );
    }

    @Test
    void inactiveAccountIsRejected() throws SQLException {
        credentialsRepo.account = Optional.of(
                account(Credentials.Status.Inactive, false)
        );

        assertTrue(
                service.login(email, password.toCharArray()).isEmpty()
        );
    }

    @Test
    void requiredPasswordChangeIsReturned() throws SQLException {
        credentialsRepo.account = Optional.of(
                account(Credentials.Status.Active, true)
        );

        var result = service.login(
                email, password.toCharArray()
        );

        assertTrue(result.isPresent());
        assertTrue(result.get().mustChangePassword());
    }

    @Test
    void rememberedLoginRejectsPasswordChangeRequirement()
            throws SQLException {

        credentialsRepo.account = Optional.of(
                account(Credentials.Status.Active, true)
        );

        assertTrue(service.restoreLogin(1).isEmpty());
    }

    @Test
    void rememberedLoginRestoresActiveAccount()
            throws SQLException {

        credentialsRepo.account = Optional.of(
                account(Credentials.Status.Active, false)
        );

        var result = service.restoreLogin(1);

        assertTrue(result.isPresent());
        assertEquals(Credentials.Role.Admin, result.get().role());
    }

    static class FakeCredentialsRepository
            extends CredentialsRepository {

        Optional<Credentials> account = Optional.empty();

        @Override
        public Optional<Credentials> findByEmail(String email) {
            return account;
        }

        @Override
        public Optional<Credentials> findById(int credentialsID) {
            return account;
        }
    }

    static class FakeUserRepository extends UserRepository {

        Optional<User> user = Optional.empty();

        @Override
        public Optional<User> findByCredentialsId(
                int credentialsID,
                Credentials.Role role) {
            return user;
        }
    }
}
