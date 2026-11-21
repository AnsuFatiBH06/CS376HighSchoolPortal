package com.highschoolportal.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * Opt-in integration tests for the shared university xbb24198 database.\n * Creates unique fixtures and deletes only their tracked IDs.\n * Requires HSPS_IT_SHARED_DB_ACK=I_ACCEPT_SHARED_DB_WRITES.
 * Existing schema must already exist in the shared database.
 *
 * Set HSPS_IT_URL, HSPS_IT_USER, HSPS_IT_PASSWORD, HSPS_IT_SHARED_DB_ACK and run:
 * mvn test "-Dtest=TeacherRepositoryIntegrationTest"
 */
@EnabledIfEnvironmentVariable(named = "HSPS_IT_URL", matches = "jdbc:mysql:.*")
class TeacherRepositoryIntegrationTest {
    private String url;
    private String username;
    private String password;
    private TeacherRepository repository;
    private final List<Integer> teacherIds = new ArrayList<>();
    private final List<Integer> credentialsIds = new ArrayList<>();
    private final List<Integer> offeringIds = new ArrayList<>();
    private final List<Integer> subjectIds = new ArrayList<>();
    private final List<Integer> yearIds = new ArrayList<>();
    private final List<Integer> managerIds = new ArrayList<>();
    private final List<Integer> schoolIds = new ArrayList<>();
    private int managerA, managerB, schoolA, schoolB, offeringA;
    private String domainA;

    @BeforeEach
    void setUp() throws SQLException {
        url = System.getenv("HSPS_IT_URL");
        username = System.getenv("HSPS_IT_USER");
        password = System.getenv("HSPS_IT_PASSWORD");
        assertNotNull(username, "Set HSPS_IT_USER");
        assertNotNull(password, "Set HSPS_IT_PASSWORD");
        assertEquals("I_ACCEPT_SHARED_DB_WRITES", System.getenv("HSPS_IT_SHARED_DB_ACK"),
                "Explicit shared-db opt-in required");
        assertEquals("jdbc:mysql://127.0.0.1:3307/xbb24198", url,
                "Only the university SSH-tunnel endpoint is allowed");

        // Validate the ACTUAL selected database, not just the URL.
        try (Connection c = open(); Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT DATABASE()")) {
            assertTrue(r.next());
            String selectedDb = r.getString(1);
            assertTrue(selectedDb != null && "xbb24198".equals(selectedDb),
                    "Refusing integration tests against non-test database: " + selectedDb);
        }
        repository = new TeacherRepository(this::open);
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        domainA = "schoola-" + suffix + ".example";
        schoolA = createSchool("School A " + suffix, domainA, "Approved");
        schoolB = createSchool("School B " + suffix, "schoolb-" + suffix + ".example", "Approved");
        managerA = createManager(schoolA, "manager-a-" + suffix + "@" + domainA, "Active");
        managerB = createManager(schoolB, "manager-b-" + suffix + "@schoolb-" + suffix + ".example", "Active");
        int yearA = insert("INSERT INTO AcademicYear (SchoolID,Name) VALUES (?,?)", schoolA, "2026-" + suffix);
        yearIds.add(yearA);
        int subjectA = insert("INSERT INTO Subject (SchoolID,Code,Name) VALUES (?,?,?)", schoolA, "S" + suffix, "Math");
        subjectIds.add(subjectA);
        offeringA = insert("INSERT INTO SubjectAcademicYear (SubjectID,AcademicYearID) VALUES (?,?)", subjectA, yearA);
        offeringIds.add(offeringA);
    }

    @AfterEach
    void cleanUp() throws SQLException {
        // Delete ONLY rows whose IDs this test created. No TRUNCATE or DROP.
        if (url == null || username == null || password == null) return;
        try (Connection c = open(); Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT DATABASE()")) {
            assertTrue(r.next() && "xbb24198".equals(r.getString(1)));
            delete(c, "SubjectAcademicYear", "SubjectAcademicYearID", offeringIds);
            delete(c, "Subject", "SubjectID", subjectIds);
            delete(c, "AcademicYear", "AcademicYearID", yearIds);
            delete(c, "Teacher", "TeacherID", teacherIds);
            delete(c, "Manager", "ManagerID", managerIds);
            delete(c, "School", "SchoolID", schoolIds);
            delete(c, "Credentials", "CredentialsID", credentialsIds);
        }
    }

    @Test
    void createsTeacherWithCredentialsAndSchoolAssociation() throws SQLException {
        var created = repository.createForManager(managerA, "John", "Smith", "john@" + domainA,
                LocalDate.of(1990, 5, 15), "dummy-bcrypt-hash");
        teacherIds.add(created.teacherID());
        credentialsIds.add(created.credentialsID());
        assertEquals(schoolA, created.schoolID());
        assertEquals(1, count("SELECT COUNT(*) FROM Teacher WHERE TeacherID = ? AND SchoolID = ?",
                created.teacherID(), schoolA));
        assertEquals(1, count("SELECT COUNT(*) FROM Credentials WHERE CredentialsID = ? " +
                "AND Role = 'Teacher' AND MustChangePassword = 1 AND Status = 'Active'",
                created.credentialsID()));
    }

    @Test
    void rejectsDuplicateEmailWithoutCreatingCredentials() throws SQLException {
        String email = "duplicate@" + domainA;
        createTrackedTeacher(managerA, email);
        int before = count("SELECT COUNT(*) FROM Credentials");
        SQLException error = assertThrows(SQLException.class, () ->
                repository.createForManager(managerA, "Another", "Teacher", email,
                        LocalDate.of(1995, 1, 1), "dummy-hash"));
        assertTrue(error.getMessage().contains("already registered"));
        assertEquals(before, count("SELECT COUNT(*) FROM Credentials"));
    }

    @Test
    void rejectsCrossSchoolAssignmentAndPreservesExistingValue() throws SQLException {
        var teacherB = createTrackedTeacher(managerB, "teacher-b@schoolb-" +
                extractSuffix(domainA) + ".example");
        assertThrows(SQLException.class, () -> repository.assignTeacherForManager(
                managerA, teacherB.teacherID(), offeringA));
        assertEquals(0, count("SELECT COUNT(*) FROM SubjectAcademicYear " +
                "WHERE SubjectAcademicYearID = ? AND TeacherID IS NOT NULL", offeringA));
    }

    @Test
    void refusesInactiveManagerForCreationAndAssignment() throws SQLException {
        var teacher = createTrackedTeacher(managerA, "valid@" + domainA);
        setManagerStatus(managerA, "Inactive");
        int before = count("SELECT COUNT(*) FROM Credentials");
        assertThrows(SQLException.class, () -> repository.createForManager(managerA, "Blocked", "User",
                "blocked@" + domainA, LocalDate.of(1990, 1, 1), "dummy-hash"));
        assertThrows(SQLException.class, () -> repository.assignTeacherForManager(
                managerA, teacher.teacherID(), offeringA));
        assertEquals(before, count("SELECT COUNT(*) FROM Credentials"));
    }

    @Test
    void rollsBackCredentialsWhenTeacherInsertFails() throws SQLException {
        // FName NOT NULL: deliberately fail AFTER the credentials INSERT.
        int before = count("SELECT COUNT(*) FROM Credentials");
        assertThrows(SQLException.class, () -> repository.createForManager(managerA, null, "Smith",
                "rollback@" + domainA, LocalDate.of(1990, 1, 1), "dummy-hash"));
        assertEquals(before, count("SELECT COUNT(*) FROM Credentials"));
        assertEquals(0, count("SELECT COUNT(*) FROM Teacher WHERE Email = ?", "rollback@" + domainA));
    }

    @Test
    void allowsReassignmentWithinSameSchool() throws SQLException {
        var a = createTrackedTeacher(managerA, "first@" + domainA);
        var b = createTrackedTeacher(managerA, "second@" + domainA);
        repository.assignTeacherForManager(managerA, a.teacherID(), offeringA);
        repository.assignTeacherForManager(managerA, b.teacherID(), offeringA);
        assertEquals(1, count("SELECT COUNT(*) FROM SubjectAcademicYear " +
                "WHERE SubjectAcademicYearID = ? AND TeacherID = ?", offeringA, b.teacherID()));
    }

    private TeacherRepository.CreatedTeacher createTrackedTeacher(int managerId, String email) throws SQLException {
        var created = repository.createForManager(managerId, "Test", "Teacher", email,
                LocalDate.of(1990, 1, 1), "dummy-hash");
        teacherIds.add(created.teacherID());
        credentialsIds.add(created.credentialsID());
        return created;
    }

    private String extractSuffix(String domain) {
        return domain.substring("schoola-".length(), domain.length() - ".example".length());
    }

    private void setManagerStatus(int managerId, String status) throws SQLException {
        try (Connection c = open(); PreparedStatement p = c.prepareStatement(
                "UPDATE Credentials SET Status = ? WHERE CredentialsID = " +
                        "(SELECT CredentialsID FROM Manager WHERE ManagerID = ?)")) {
            p.setString(1, status);
            p.setInt(2, managerId);
            p.executeUpdate();
        }
    }

    private int createSchool(String name, String domain, String status) throws SQLException {
        int id = insert("INSERT INTO School (Name,Email,EmailDomain,Address,Status) VALUES (?,?,?,?,?)",
                name, "office@" + domain, domain, "Test address", status);
        schoolIds.add(id);
        return id;
    }

    private int createManager(int schoolId, String email, String status) throws SQLException {
        int cred = insert("INSERT INTO Credentials (PasswordHash,Role,Status) VALUES (?,'Manager',?)",
                "dummy-hash", status);
        credentialsIds.add(cred);
        int manager = insert("INSERT INTO Manager (FName,LName,Email,SchoolID,CredentialsID) " +
                "VALUES (?,?,?,?,?)", "Test", "Manager", email, schoolId, cred);
        managerIds.add(manager);
        return manager;
    }

    private Connection open() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    private int insert(String sql, Object... parameters) throws SQLException {
        try (Connection c = open(); PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(p, parameters);
            assertEquals(1, p.executeUpdate());
            try (ResultSet keys = p.getGeneratedKeys()) {
                assertTrue(keys.next(), "No generated key for: " + sql);
                return keys.getInt(1);
            }
        }
    }

    private int count(String sql, Object... parameters) throws SQLException {
        try (Connection c = open(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, parameters);
            try (ResultSet r = p.executeQuery()) {
                assertTrue(r.next());
                return r.getInt(1);
            }
        }
    }

    private static void bind(PreparedStatement p, Object... values) throws SQLException {
        for (int i = 0; i < values.length; i++) p.setObject(i + 1, values[i]);
    }

    private static void delete(Connection c, String table, String key, List<Integer> ids) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("DELETE FROM " + table + " WHERE " + key + " = ?")) {
            for (int id : ids) {
                p.setInt(1, id);
                p.executeUpdate();
            }
        }
    }
}
