package com.highschoolportal.repository;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named="HSPS_IT_SHARED_DB_ACK", matches="I_ACCEPT_SHARED_DB_WRITES")
class AttendanceRepositoryIntegrationTest {
    private static final String URL = "jdbc:mysql://127.0.0.1:3307/xbb24198";
    private String user, password;
    private AttendanceRepository repo;
    private final List<Fixture> fixtures = new ArrayList<>();
    private record Fixture(String table, String key, int id) {}
    private int teacher, otherTeacher, foreignTeacher, student, otherStudent, detail, credential;
    private final LocalDate day = LocalDate.of(2026, 1, 12);
    private boolean verified;

    private Connection open() throws SQLException {
        return DriverManager.getConnection(URL, user, password);
    }
    @BeforeEach void setup() throws SQLException {
        assertEquals(URL, System.getenv("HSPS_IT_URL"));
        user = System.getenv("HSPS_IT_USER"); password = System.getenv("HSPS_IT_PASSWORD");
        assertEquals("xbb24198", user); assertNotNull(password);
        try (Connection c = open(); Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT DATABASE()")) {
            assertTrue(r.next()); assertEquals("xbb24198", r.getString(1));
        }
        verified = true;
        repo = new AttendanceRepository(this::open);
        String suffix = UUID.randomUUID().toString();
        try (Connection c = open()) {
            c.setAutoCommit(false);
            try {
                int school = school(c, "a-"+suffix), foreign = school(c, "b-"+suffix);
                credential = credentials(c, "Teacher");
                teacher = person(c,"Teacher",school,credential,"t-"+suffix);
                otherTeacher = person(c,"Teacher",school,credentials(c,"Teacher"),"o-"+suffix);
                foreignTeacher = person(c,"Teacher",foreign,credentials(c,"Teacher"),"f-"+suffix);
                student = person(c,"Student",school,credentials(c,"Student"),"s-"+suffix);
                otherStudent = person(c,"Student",school,credentials(c,"Student"),"x-"+suffix);
                int year = insert(c,"AcademicYear","AcademicYearID","SchoolID,Name",school,"IT-"+suffix);
                int subject = insert(c,"Subject","SubjectID","SchoolID,Code,Name",school,"IT-"+suffix,"Test mathematics");
                int offering = insert(c,"SubjectAcademicYear","SubjectAcademicYearID","SubjectID,AcademicYearID,TeacherID",subject,year,teacher);
                int enrollment = insert(c,"StudentAcademicYear","StudentAcademicYearID","StudentID,AcademicYearID,Grade",student,year,"10");
                detail = insert(c,"Details","DetailID","StudentAcademicYearID,SubjectAcademicYearID",enrollment,offering);
                c.commit();
            } catch (SQLException | RuntimeException | Error ex) { c.rollback(); fixtures.clear(); throw ex; }
        }
    }
    private int school(Connection c,String suffix) throws SQLException {
        return insert(c,"School","SchoolID","Name,Email,EmailDomain,Address,Status","IT-"+suffix,"office@"+suffix+".example",suffix+".example","Test only","Approved");
    }
    private int credentials(Connection c,String role) throws SQLException {
        return insert(c,"Credentials","CredentialsID","PasswordHash,Role,MustChangePassword,Status","unusable-test-hash",role,0,"Active");
    }
    private int person(Connection c,String table,int school,int cred,String suffix) throws SQLException {
        return insert(c,table,table+"ID","FName,LName,Email,DOB,SchoolID,CredentialsID","Test","Fixture",suffix+"@example.invalid",LocalDate.of(2000,1,1),school,cred);
    }
    private int insert(Connection c,String table,String key,String columns,Object... values) throws SQLException {
        String placeholders = String.join(",",Collections.nCopies(values.length,"?"));
        try (PreparedStatement p = c.prepareStatement("INSERT INTO "+table+" ("+columns+") VALUES ("+placeholders+")",Statement.RETURN_GENERATED_KEYS)) {
            for(int i=0;i<values.length;i++) p.setObject(i+1,values[i]);
            assertEquals(1,p.executeUpdate());
            try(ResultSet r=p.getGeneratedKeys()) { assertTrue(r.next()); int id=r.getInt(1); fixtures.add(new Fixture(table,key,id)); return id; }
        }
    }
    private void update(String sql,Object... values) throws SQLException {
        try(Connection c=open(); PreparedStatement p=c.prepareStatement(sql)) {
            for(int i=0;i<values.length;i++) p.setObject(i+1,values[i]); p.executeUpdate();
        }
    }
    @AfterEach void cleanup() throws SQLException {
        if(!verified || fixtures.isEmpty()) return;
        try(Connection c=open()) {
            c.setAutoCommit(false);
            try {
                if(detail>0) try(PreparedStatement p=c.prepareStatement("DELETE FROM Attendance WHERE DetailID=?")) { p.setInt(1,detail); p.executeUpdate(); }
                for(int i=fixtures.size()-1;i>=0;i--) {
                    Fixture f=fixtures.get(i);
                    try(PreparedStatement p=c.prepareStatement("DELETE FROM "+f.table()+" WHERE "+f.key()+"=?")) { p.setInt(1,f.id()); p.executeUpdate(); }
                }
                c.commit();
            } catch(SQLException ex) { c.rollback(); throw ex; }
        }
    }
    private int submit() throws SQLException { return repo.insertPendingAttendance(student,detail,day,3,"Present"); }
    private boolean pending(int who,int id) throws SQLException { return repo.listPendingForTeacher(who).stream().anyMatch(a->a.attendanceID()==id); }
    private String status() throws SQLException { return repo.listHistory(student).stream().filter(a->a.sessionDate().equals(day)).findFirst().orElseThrow().approvalStatus(); }
    @Test void studentSubmissionAndApproval() throws SQLException {
        assertTrue(repo.listEnrolledSubjects(student).stream().anyMatch(a->a.detailID()==detail));
        int id=submit(); assertEquals("Pending",status()); assertTrue(pending(teacher,id));
        repo.reviewPendingAttendance(teacher,id,"Approved"); assertEquals("Approved",status()); assertFalse(pending(teacher,id));
    }
    @Test void rejectionCannotBeOverwritten() throws SQLException {
        int id=submit(); repo.reviewPendingAttendance(teacher,id,"Rejected");
        assertThrows(SQLException.class,()->repo.reviewPendingAttendance(teacher,id,"Approved")); assertEquals("Rejected",status());
    }
    @Test void duplicateSubmissionRejected() throws SQLException {
        submit(); assertThrows(SQLException.class,this::submit); assertEquals(1,repo.listHistory(student).size());
    }
    @Test void anotherStudentCannotSubmit() throws SQLException {
        assertThrows(SQLException.class,()->repo.insertPendingAttendance(otherStudent,detail,day,3,"Present")); assertTrue(repo.listHistory(student).isEmpty());
    }
    @Test void unauthorizedTeachersCannotReview() throws SQLException {
        int id=submit();
        for(int who:new int[]{otherTeacher,foreignTeacher}) { assertFalse(pending(who,id)); assertThrows(SQLException.class,()->repo.reviewPendingAttendance(who,id,"Approved")); }
        assertEquals("Pending",status());
    }
    @Test void frozenAndInactiveRecordsProtected() throws SQLException {
        int id=submit(); update("UPDATE Details SET IsFrozen=1 WHERE DetailID=?",detail);
        assertFalse(pending(teacher,id)); assertThrows(SQLException.class,()->repo.reviewPendingAttendance(teacher,id,"Approved"));
        assertThrows(SQLException.class,()->repo.insertPendingAttendance(student,detail,day.plusDays(1),3,"Late"));
        update("UPDATE Details SET IsFrozen=0 WHERE DetailID=?",detail);
        update("UPDATE Credentials SET Status='Inactive' WHERE CredentialsID=?",credential);
        assertFalse(pending(teacher,id)); assertThrows(SQLException.class,()->repo.reviewPendingAttendance(teacher,id,"Approved")); assertEquals("Pending",status());
    }
}
