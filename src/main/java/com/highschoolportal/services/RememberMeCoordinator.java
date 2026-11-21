
package com.highschoolportal.services;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.OptionalInt;

public class RememberMeCoordinator {

    private final RememberMeService tokens =
            new RememberMeService();

    private final LocalTokenStore store =
            new LocalTokenStore();

    private final AuthenticationService auth =
            new AuthenticationService();

    public void remember(int credentialsID)
            throws SQLException, IOException {

        String token = tokens.createToken(credentialsID);

        try {
            store.save(token);
        } catch (IOException e) {
            tokens.revokeToken(token);
            throw e;
        }
    }

    public Optional<AuthenticationService.LoginResult> restore()
            throws SQLException, IOException {

        Optional<String> saved = store.load();

        if (saved.isEmpty()) {
            return Optional.empty();
        }

        String oldToken = saved.get();

        OptionalInt id = tokens.validateToken(oldToken);

        if (id.isEmpty()) {
            store.delete();
            return Optional.empty();
        }

        Optional<AuthenticationService.LoginResult> result =
                auth.restoreLogin(id.getAsInt());

        if (result.isEmpty()) {
            tokens.revokeToken(oldToken);
            store.delete();
            return Optional.empty();
        }

        // Rotate the token after successful restoration
        remember(id.getAsInt());
        tokens.revokeToken(oldToken);

        return result;
    }

    public void forget() throws SQLException, IOException {
        Optional<String> saved = store.load();

        store.delete();

        if (saved.isPresent()) {
            tokens.revokeToken(saved.get());
        }
    }
}
