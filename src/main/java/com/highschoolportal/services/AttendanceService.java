package com.highschoolportal.services;

import com.highschoolportal.repository.AttendanceRepository;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Objects;

/** UR-F36: students record attendance for their own enrolled subjects. */
public class AttendanceService {
    public enum AttendanceStatus { Present, Late, Absent }

    private final AttendanceRepository repository;
    private final Clock clock;

    public AttendanceService() {
        this(new AttendanceRepository(), Clock.systemDefaultZone());
    }

    public AttendanceService(AttendanceRepository repository) {
        this(repository, Clock.systemDefaultZone());
    }

    public AttendanceService(AttendanceRepository repository, Clock clock) {
        this.repository = Objects.requireNonNull(repository);
        this.clock = Objects.requireNonNull(clock);
    }

    public List<AttendanceRepository.EnrolledSubject> listSubjects(int studentID) throws SQLException {
        if (studentID <= 0) throw new IllegalArgumentException("A logged-in student is required");
        return repository.listEnrolledSubjects(studentID);
    }

    public int recordAttendance(int studentID, int detailID, LocalDate sessionDate,
                                AttendanceStatus status) throws SQLException {
        if (studentID <= 0 || detailID <= 0) {
            throw new IllegalArgumentException("A logged-in student and enrolled subject are required");
        }
        if (sessionDate == null || sessionDate.isAfter(LocalDate.now(clock))) {
            throw new IllegalArgumentException("Attendance date must not be in the future");
        }
        if (status == null) throw new IllegalArgumentException("Choose an attendance status");
        int weekNumber = sessionDate.get(WeekFields.ISO.weekOfWeekBasedYear());
        return repository.insertPendingAttendance(studentID, detailID,
                sessionDate, weekNumber, status.name());
    }
    public List<AttendanceRepository.AttendanceEntry> listHistory(int studentID) throws SQLException {
        if (studentID <= 0) throw new IllegalArgumentException("A logged-in student is required");
        return repository.listHistory(studentID);
    }

    public enum ReviewDecision { Approved, Rejected }

    public List<AttendanceRepository.PendingAttendance> listPendingForTeacher(int teacherID)
            throws SQLException {
        if (teacherID <= 0) throw new IllegalArgumentException("A logged-in teacher is required");
        return repository.listPendingForTeacher(teacherID);
    }

    public void reviewAttendance(int teacherID, int attendanceID, ReviewDecision decision)
            throws SQLException {
        if (teacherID <= 0 || attendanceID <= 0) {
            throw new IllegalArgumentException("A logged-in teacher and attendance record are required");
        }
        if (decision == null) throw new IllegalArgumentException("Choose an approval decision");
        repository.reviewPendingAttendance(teacherID, attendanceID, decision.name());
    }
}
