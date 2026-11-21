package com.highschoolportal.services;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.highschoolportal.model.Manager;
import com.highschoolportal.repository.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests only: fake repository, no MariaDB connection or live test records. */
class TeacherManagementServiceTest {
    private FakeTeacherRepository repository;
    private TeacherManagementService service;
    private Manager manager;

    @BeforeEach
    void setUp() {
        repository = new FakeTeacherRepository();
        service = new TeacherManagementService(repository);
        manager = new Manager(5, "Test", "Manager", "manager@example.test", 9, null, 12);
    }

    private TeacherRepository.CreatedTeacher create(Manager actingManager, String first, String last,
                                                     String email, LocalDate dob, String password)
            throws SQLException {
        return service.createTeacher(actingManager, first, last, email, dob, password.toCharArray());
    }

    @Test
    void createTeacherTrimsFieldsAndHashesPassword() throws SQLException {
        var created = create(manager, "  Jane  ", " Smith ", " jane@example.test ",
                LocalDate.of(1990, 1, 1), "TempPassword#2026");

        assertEquals(new TeacherRepository.CreatedTeacher(101, 202, 12), created);
        assertEquals(5, repository.createdManagerID);
        assertEquals("Jane", repository.createdFirst);
        assertEquals("Smith", repository.createdLast);
        assertEquals("jane@example.test", repository.createdEmail);
        assertNotEquals("TempPassword#2026", repository.createdHash);
        assertTrue(BCrypt.verifyer().verify("TempPassword#2026".toCharArray(),
                repository.createdHash).verified);
    }

    @Test
    void rejectsMissingManager() {
        assertThrows(IllegalArgumentException.class, () -> create(null, "Jane", "Smith",
                "jane@example.test", LocalDate.of(1990, 1, 1), "TempPassword#2026"));
        assertEquals(0, repository.createCalls);
    }

    @Test
    void rejectsManagerWithoutSchool() {
        var unassigned = new Manager(5, "Test", "Manager", "manager@example.test", 9, null);
        assertThrows(IllegalArgumentException.class, () -> create(unassigned, "Jane", "Smith",
                "jane@example.test", LocalDate.of(1990, 1, 1), "TempPassword#2026"));
        assertEquals(0, repository.createCalls);
    }

    @Test
    void rejectsBlankNames() {
        assertThrows(IllegalArgumentException.class, () -> create(manager, " ", "Smith",
                "jane@example.test", LocalDate.of(1990, 1, 1), "TempPassword#2026"));
        assertThrows(IllegalArgumentException.class, () -> create(manager, "Jane", " ",
                "jane@example.test", LocalDate.of(1990, 1, 1), "TempPassword#2026"));
        assertEquals(0, repository.createCalls);
    }

    @Test
    void rejectsMalformedEmail() {
        assertThrows(IllegalArgumentException.class, () -> create(manager, "Jane", "Smith",
                "not-an-email", LocalDate.of(1990, 1, 1), "TempPassword#2026"));
        assertEquals(0, repository.createCalls);
    }

    @Test
    void rejectsInvalidDatesOfBirth() {
        assertThrows(IllegalArgumentException.class, () -> create(manager, "Jane", "Smith",
                "jane@example.test", LocalDate.now(), "TempPassword#2026"));
        assertThrows(IllegalArgumentException.class, () -> create(manager, "Jane", "Smith",
                "jane@example.test", LocalDate.now().plusDays(1), "TempPassword#2026"));
        assertEquals(0, repository.createCalls);
    }

    @Test
    void rejectsShortAndOverlongPasswords() {
        assertThrows(IllegalArgumentException.class, () -> create(manager, "Jane", "Smith",
                "jane@example.test", LocalDate.of(1990, 1, 1), "short"));
        assertThrows(IllegalArgumentException.class, () -> create(manager, "Jane", "Smith",
                "jane@example.test", LocalDate.of(1990, 1, 1), "x".repeat(73)));
        assertEquals(0, repository.createCalls);
    }

    @Test
    void repositoryCreationFailurePropagates() {
        repository.createFailure = new SQLException("Duplicate email");
        var thrown = assertThrows(SQLException.class, () -> create(manager, "Jane", "Smith",
                "jane@example.test", LocalDate.of(1990, 1, 1), "TempPassword#2026"));
        assertEquals("Duplicate email", thrown.getMessage());
    }

    @Test
    void getTeachersDelegatesWithManagerId() throws SQLException {
        var teachers = service.getTeachers(manager);
        assertEquals(1, teachers.size());
        assertEquals(5, repository.listTeachersManagerID);
    }

    @Test
    void getSubjectOfferingsDelegatesWithManagerId() throws SQLException {
        var offerings = service.getSubjectOfferings(manager);
        assertEquals(1, offerings.size());
        assertEquals(5, repository.listOfferingsManagerID);
    }

    @Test
    void assignmentForwardsAllIds() throws SQLException {
        service.assignTeacher(manager, 21, 31);
        assertEquals(5, repository.assignedManagerID);
        assertEquals(21, repository.assignedTeacherID);
        assertEquals(31, repository.assignedOfferingID);
    }

    @Test
    void rejectsInvalidAssignmentIdsBeforeRepositoryCall() {
        assertThrows(IllegalArgumentException.class, () -> service.assignTeacher(manager, 0, 31));
        assertThrows(IllegalArgumentException.class, () -> service.assignTeacher(manager, 21, -1));
        assertEquals(0, repository.assignmentCalls);
    }

    @Test
    void rejectsInvalidManagersForAssignmentAndListings() {
        assertThrows(IllegalArgumentException.class, () -> service.assignTeacher(null, 21, 31));
        assertThrows(IllegalArgumentException.class, () -> service.getTeachers(null));
        assertThrows(IllegalArgumentException.class, () -> service.getSubjectOfferings(null));
        assertEquals(0, repository.assignmentCalls);
    }

    @Test
    void repositoryAssignmentRejectionPropagates() {
        repository.assignmentFailure = new SQLException("Assignment rejected: school mismatch");
        var thrown = assertThrows(SQLException.class, () -> service.assignTeacher(manager, 21, 31));
        assertTrue(thrown.getMessage().contains("school mismatch"));
    }

    private static class FakeTeacherRepository extends TeacherRepository {
        int createCalls;
        int createdManagerID;
        String createdFirst, createdLast, createdEmail, createdHash;
        SQLException createFailure;
        int listTeachersManagerID, listOfferingsManagerID;
        int assignmentCalls, assignedManagerID, assignedTeacherID, assignedOfferingID;
        SQLException assignmentFailure;

        @Override
        public CreatedTeacher createForManager(int managerID, String firstName, String lastName,
                                                String email, LocalDate dob, String passwordHash)
                throws SQLException {
            createCalls++;
            if (createFailure != null) throw createFailure;
            createdManagerID = managerID;
            createdFirst = firstName;
            createdLast = lastName;
            createdEmail = email;
            createdHash = passwordHash;
            return new CreatedTeacher(101, 202, 12);
        }

        @Override
        public List<TeacherOption> listTeachersForManager(int managerID) {
            listTeachersManagerID = managerID;
            return List.of(new TeacherOption(21, "Jane Smith"));
        }

        @Override
        public List<SubjectOfferingOption> listSubjectOfferingsForManager(int managerID) {
            listOfferingsManagerID = managerID;
            return List.of(new SubjectOfferingOption(31, "2026/2027 | MATH - Mathematics", null));
        }

        @Override
        public void assignTeacherForManager(int managerID, int teacherID, int subjectAcademicYearID)
                throws SQLException {
            assignmentCalls++;
            if (assignmentFailure != null) throw assignmentFailure;
            assignedManagerID = managerID;
            assignedTeacherID = teacherID;
            assignedOfferingID = subjectAcademicYearID;
        }
    }
}
