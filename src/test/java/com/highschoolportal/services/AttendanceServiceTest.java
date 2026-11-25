package com.highschoolportal.services;

import com.highschoolportal.repository.AttendanceRepository;
import org.junit.jupiter.api.Test;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AttendanceServiceTest {
    static class FakeRepository extends AttendanceRepository {
        int calls;
        int studentID, detailID, week;
        String status;
        @Override public int insertPendingAttendance(int studentID, int detailID,
                LocalDate sessionDate, int weekNumber, String status) {
            calls++;
            this.studentID = studentID;
            this.detailID = detailID;
            this.week = weekNumber;
            this.status = status;
            return 42;
        }
        @Override public List<EnrolledSubject> listEnrolledSubjects(int studentID) {
            return List.of(new EnrolledSubject(7, "MATH101", "Mathematics", "2026/2027"));
        }
    }
    private final FakeRepository repo = new FakeRepository();
    private final AttendanceService service = new AttendanceService(repo,
            Clock.fixed(Instant.parse("2026-11-25T12:00:00Z"), ZoneOffset.UTC));

    @Test void submitsPendingAttendanceWithIsoWeek() throws SQLException {
        assertEquals(42, service.recordAttendance(3, 7, LocalDate.of(2026, 11, 23),
                AttendanceService.AttendanceStatus.Present));
        assertEquals(1, repo.calls);
        assertEquals(3, repo.studentID);
        assertEquals(7, repo.detailID);
        assertEquals(48, repo.week);
        assertEquals("Present", repo.status);
    }

    @Test void rejectsInvalidStudent() {
        assertThrows(IllegalArgumentException.class,
                () -> service.recordAttendance(0, 7, LocalDate.of(2026, 11, 23),
                        AttendanceService.AttendanceStatus.Late));
        assertEquals(0, repo.calls);
    }

    @Test void rejectsInvalidEnrollment() {
        assertThrows(IllegalArgumentException.class,
                () -> service.recordAttendance(3, 0, LocalDate.of(2026, 11, 23),
                        AttendanceService.AttendanceStatus.Late));
        assertEquals(0, repo.calls);
    }

    @Test void rejectsFutureDate() {
        assertThrows(IllegalArgumentException.class,
                () -> service.recordAttendance(3, 7, LocalDate.of(2026, 11, 26),
                        AttendanceService.AttendanceStatus.Absent));
        assertEquals(0, repo.calls);
    }

    @Test void rejectsNullDateOrStatus() {
        assertThrows(IllegalArgumentException.class,
                () -> service.recordAttendance(3, 7, null,
                        AttendanceService.AttendanceStatus.Absent));
        assertThrows(IllegalArgumentException.class,
                () -> service.recordAttendance(3, 7, LocalDate.of(2026, 11, 23), null));
        assertEquals(0, repo.calls);
    }

    @Test void listsEnrolledSubjects() throws SQLException {
        assertEquals("MATH101", service.listSubjects(3).getFirst().subjectCode());
        assertThrows(IllegalArgumentException.class, () -> service.listSubjects(-1));
    }
}
