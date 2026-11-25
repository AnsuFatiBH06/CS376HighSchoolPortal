package com.highschoolportal.services;

import com.highschoolportal.repository.AttendanceRepository;
import com.highschoolportal.repository.AttendanceRepository.PendingAttendance;
import com.highschoolportal.services.AttendanceService.ReviewDecision;
import org.junit.jupiter.api.Test;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TeacherAttendanceServiceTest {
    private static class FakeRepository extends AttendanceRepository {
        int listCalls, reviewCalls, teacherID, attendanceID;
        String decision;
        SQLException failure;
        List<PendingAttendance> pending = List.of(new PendingAttendance(
                42, 7, 3, "Test Student", "MATH101", "Mathematics",
                "2026/2027", LocalDate.of(2026, 11, 23), 48, "Present"));

        @Override public List<PendingAttendance> listPendingForTeacher(int teacherID)
                throws SQLException {
            listCalls++;
            this.teacherID = teacherID;
            if (failure != null) throw failure;
            return pending;
        }

        @Override public void reviewPendingAttendance(int teacherID, int attendanceID,
                String decision) throws SQLException {
            reviewCalls++;
            this.teacherID = teacherID;
            this.attendanceID = attendanceID;
            this.decision = decision;
            if (failure != null) throw failure;
        }
    }

    private final FakeRepository repo = new FakeRepository();
    private final AttendanceService service = new AttendanceService(repo);

    @Test void listsPendingAttendanceForTeacher() throws SQLException {
        assertEquals(repo.pending, service.listPendingForTeacher(5));
        assertEquals(5, repo.teacherID);
        assertEquals(1, repo.listCalls);
    }

    @Test void returnsEmptyPendingList() throws SQLException {
        repo.pending = List.of();
        assertTrue(service.listPendingForTeacher(5).isEmpty());
        assertEquals(1, repo.listCalls);
    }

    @Test void rejectsInvalidTeacherWhenListing() {
        for (int id : new int[]{0, -1}) {
            assertThrows(IllegalArgumentException.class, () -> service.listPendingForTeacher(id));
        }
        assertEquals(0, repo.listCalls);
    }

    @Test void approvesAttendance() throws SQLException {
        service.reviewAttendance(5, 42, ReviewDecision.Approved);
        assertEquals(1, repo.reviewCalls);
        assertEquals(5, repo.teacherID);
        assertEquals(42, repo.attendanceID);
        assertEquals("Approved", repo.decision);
    }

    @Test void rejectsAttendance() throws SQLException {
        service.reviewAttendance(5, 42, ReviewDecision.Rejected);
        assertEquals(1, repo.reviewCalls);
        assertEquals(5, repo.teacherID);
        assertEquals(42, repo.attendanceID);
        assertEquals("Rejected", repo.decision);
    }

    @Test void rejectsInvalidTeacherWhenReviewing() {
        for (int id : new int[]{0, -1}) {
            assertThrows(IllegalArgumentException.class,
                    () -> service.reviewAttendance(id, 42, ReviewDecision.Approved));
        }
        assertEquals(0, repo.reviewCalls);
    }

    @Test void rejectsInvalidAttendanceID() {
        for (int id : new int[]{0, -1}) {
            assertThrows(IllegalArgumentException.class,
                    () -> service.reviewAttendance(5, id, ReviewDecision.Approved));
        }
        assertEquals(0, repo.reviewCalls);
    }

    @Test void rejectsMissingDecision() {
        assertThrows(IllegalArgumentException.class,
                () -> service.reviewAttendance(5, 42, null));
        assertEquals(0, repo.reviewCalls);
    }

    @Test void propagatesListingFailure() {
        repo.failure = new SQLException("Database unavailable");
        assertSame(repo.failure, assertThrows(SQLException.class,
                () -> service.listPendingForTeacher(5)));
        assertEquals(1, repo.listCalls);
    }

    @Test void propagatesReviewFailure() {
        repo.failure = new SQLException("Attendance no longer pending or not accessible");
        assertSame(repo.failure, assertThrows(SQLException.class,
                () -> service.reviewAttendance(5, 42, ReviewDecision.Approved)));
        assertEquals(1, repo.reviewCalls);
    }
}
