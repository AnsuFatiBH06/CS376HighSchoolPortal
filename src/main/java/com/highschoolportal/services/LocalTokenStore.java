
package com.highschoolportal.services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class LocalTokenStore {

    private final Path directory = Path.of(
            System.getProperty("user.home"), ".hsps"
    );

    private final Path tokenFile =
            directory.resolve("remember-token");

    public void save(String token) throws IOException {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Invalid token");
        }

        Files.createDirectories(directory);
        restrictAccess(directory);

        Files.writeString(
                tokenFile,
                token,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );

        try {
            restrictAccess(tokenFile);
        } catch (IOException e) {
            Files.deleteIfExists(tokenFile);
            throw e;
        }
    }

    public Optional<String> load() throws IOException {
        if (!Files.isRegularFile(tokenFile)) {
            return Optional.empty();
        }

        restrictAccess(tokenFile);

        String token = Files.readString(
                tokenFile,
                StandardCharsets.UTF_8
        ).trim();

        return token.isEmpty()
                ? Optional.empty()
                : Optional.of(token);
    }

    public void delete() throws IOException {
        Files.deleteIfExists(tokenFile);
    }

    private void restrictAccess(Path path) throws IOException {
        PosixFileAttributeView posix =
                Files.getFileAttributeView(
                        path, PosixFileAttributeView.class
                );

        if (posix != null) {
            Files.setPosixFilePermissions(
                    path,
                    Set.of(
                            PosixFilePermission.OWNER_READ,
                            PosixFilePermission.OWNER_WRITE,
                            PosixFilePermission.OWNER_EXECUTE
                    )
            );
            return;
        }

        AclFileAttributeView acl =
                Files.getFileAttributeView(
                        path, AclFileAttributeView.class
                );

        if (acl == null) {
            throw new IOException(
                    "Cannot restrict token file permissions"
            );
        }

        UserPrincipal owner = Files.getOwner(path);

        AclEntry entry = AclEntry.newBuilder()
                .setType(AclEntryType.ALLOW)
                .setPrincipal(owner)
                .setPermissions(
                        AclEntryPermission.values()
                )
                .build();

        acl.setAcl(List.of(entry));
    }
}
