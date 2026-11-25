package com.highschoolportal.repository;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named="HSPS_IT_SHARED_DB_ACK", matches="I_ACCEPT_SHARED_DB_WRITES")
class ExamMarkRepositoryIntegrationTest {
    private static final String URL = "jdbc:mysql://127.0.0.1:3307/xbb24198";
    private String user, password;
    private ExamMarkRepository repo;
    private final List<Fixture> fixtures = new ArrayList<>();
    private record Fixture(String table, String key, int id) {}
    private int teacher, otherTeacher, foreignTeacher, student, otherStudent, detail, credential, otherCredential, foreignCredential, year, offering;
    private static java.math.BigDecimal n(String value) { return new java.math.BigDecimal(value); }
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
        repo = new ExamMarkRepository(this::open);
        String suffix = UUID.randomUUID().toString();
        try (Connection c = open()) {
            c.setAutoCommit(false);
            try {
                int school = school(c, "a-"+suffix), foreign = school(c, "b-"+suffix);
                credential = credentials(c, "Teacher");
                teacher = person(c,"Teacher",school,credential,"t-"+suffix);
                otherCredential=credentials(c,"Teacher");
                otherTeacher = person(c,"Teacher",school,otherCredential,"o-"+suffix);
                foreignCredential=credentials(c,"Teacher");
                foreignTeacher = person(c,"Teacher",foreign,foreignCredential,"f-"+suffix);
                student = person(c,"Student",school,credentials(c,"Student"),"s-"+suffix);
                otherStudent = person(c,"Student",school,credentials(c,"Student"),"x-"+suffix);
                year = insert(c,"AcademicYear","AcademicYearID","SchoolID,Name,ExamPeriodOpen",school,"IT-"+suffix,1);
                int subject = insert(c,"Subject","SubjectID","SchoolID,Code,Name",school,"IT-"+suffix,"Test mathematics");
                offering = insert(c,"SubjectAcademicYear","SubjectAcademicYearID","SubjectID,AcademicYearID,TeacherID,PassMark,MaxMark,ExamSeriesNumber",subject,year,teacher,n("50"),n("100"),2);
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
                if(detail>0) try(PreparedStatement p=c.prepareStatement("DELETE FROM ExamMark WHERE DetailID=?")) { p.setInt(1,detail); p.executeUpdate(); }
                if(offering>0) try(PreparedStatement p=c.prepareStatement("DELETE FROM Exam WHERE SubjectAcademicYearID=?")) { p.setInt(1,offering); p.executeUpdate(); }
                for(int i=fixtures.size()-1;i>=0;i--) {
                    Fixture f=fixtures.get(i);
                    try(PreparedStatement p=c.prepareStatement("DELETE FROM "+f.table()+" WHERE "+f.key()+"=?")) { p.setInt(1,f.id()); p.executeUpdate(); }
                }
                c.commit();
            } catch(SQLException ex) { c.rollback(); throw ex; }
        }
    }
    private com.highschoolportal.model.ExamMarkRules.Result save(int series,String mark) throws SQLException { return repo.saveMark(teacher,credential,detail,series,n(mark)); }
    private ExamMarkRepository.MarkEntry entry() throws SQLException { return repo.listForTeacher(teacher,credential).stream().filter(e->e.detailID()==detail).findFirst().orElseThrow(); }
    @Test void storesSeriesAndCalculatesFinalMark() throws SQLException {
        assertTrue(entry().editable()); assertTrue(entry().marks().isEmpty());
        assertEquals("Incomplete",save(1,"30").status());assertNull(entry().finalMark());
        assertEquals("Passed",save(2,"25").status());assertEquals(n("55.00"),entry().finalMark());assertEquals(2,entry().marks().size());assertEquals("Passed",entry().resultStatus());
    }
    @Test void editingSeriesRecalculatesWithoutDuplicate() throws SQLException {
        save(1,"30");save(2,"25");assertEquals("Failed",save(2,"10").status());
        assertEquals(n("40.00"),entry().finalMark());assertEquals(n("10.00"),entry().marks().get(2));assertEquals(2,entry().marks().size());
    }
    @Test void rejectsExcessTotalAtomically() throws SQLException {
        save(1,"60");assertThrows(IllegalArgumentException.class,()->save(2,"41"));assertEquals(1,entry().marks().size());assertNull(entry().finalMark());
        save(2,"40");assertEquals(n("100.00"),entry().finalMark());assertThrows(IllegalArgumentException.class,()->save(1,"61"));assertEquals(n("60.00"),entry().marks().get(1));assertEquals(n("100.00"),entry().finalMark());
    }
    @Test void preventsUnauthorizedTeachersAndCredentialMismatch() throws SQLException {
        assertTrue(repo.listForTeacher(otherTeacher,otherCredential).isEmpty());assertTrue(repo.listForTeacher(foreignTeacher,foreignCredential).isEmpty());assertTrue(repo.listForTeacher(teacher,otherCredential).isEmpty());
        assertThrows(SQLException.class,()->repo.saveMark(otherTeacher,otherCredential,detail,1,n("20")));
        assertThrows(SQLException.class,()->repo.saveMark(foreignTeacher,foreignCredential,detail,1,n("20")));
        assertThrows(SQLException.class,()->repo.saveMark(teacher,otherCredential,detail,1,n("20")));assertTrue(entry().marks().isEmpty());
    }
    @Test void blocksClosedPeriodAndFrozenEnrolment() throws SQLException {
        update("UPDATE AcademicYear SET ExamPeriodOpen=0 WHERE AcademicYearID=?",year);assertFalse(entry().editable());assertThrows(SQLException.class,()->save(1,"20"));
        update("UPDATE AcademicYear SET ExamPeriodOpen=1 WHERE AcademicYearID=?",year);update("UPDATE Details SET IsFrozen=1 WHERE DetailID=?",detail);assertFalse(entry().editable());assertThrows(SQLException.class,()->save(1,"20"));assertTrue(entry().marks().isEmpty());
    }
    @Test void rejectsInvalidSeriesAndInactiveTeacher() throws SQLException {
        assertThrows(IllegalArgumentException.class,()->save(3,"20"));assertTrue(entry().marks().isEmpty());
        update("UPDATE Credentials SET Status='Inactive' WHERE CredentialsID=?",credential);assertTrue(repo.listForTeacher(teacher,credential).isEmpty());assertThrows(SQLException.class,()->save(1,"20"));
    }
}
