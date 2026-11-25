package com.highschoolportal.repository;

import com.highschoolportal.database.MySQLConnect;
import com.highschoolportal.model.ExamMarkRules;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

/** Teacher exam marking with database ownership checks and atomic result updates. */
public class ExamMarkRepository {
    @FunctionalInterface public interface ConnectionProvider { Connection open() throws SQLException; }
    private final ConnectionProvider connections;
    public ExamMarkRepository() { this(() -> MySQLConnect.getInstance().getConnection()); }
    public ExamMarkRepository(ConnectionProvider connections) { this.connections = Objects.requireNonNull(connections); }

    public record MarkEntry(int detailID, String student, String subject, String academicYear,
                            int seriesCount, BigDecimal passMark, BigDecimal maxMark,
                            boolean examOpen, boolean frozen, Map<Integer, BigDecimal> marks,
                            BigDecimal finalMark, String resultStatus) {
        public MarkEntry { marks = Collections.unmodifiableMap(new TreeMap<>(marks)); }
        public boolean editable() { return examOpen && !frozen && ExamMarkRules.validConfiguration(seriesCount, passMark, maxMark); }
    }

    private static final String BASE = """
        FROM Details d
        JOIN StudentAcademicYear enrol ON enrol.StudentAcademicYearID=d.StudentAcademicYearID
        JOIN Student st ON st.StudentID=enrol.StudentID
        JOIN SubjectAcademicYear offering ON offering.SubjectAcademicYearID=d.SubjectAcademicYearID
        JOIN Subject subj ON subj.SubjectID=offering.SubjectID
        JOIN AcademicYear ay ON ay.AcademicYearID=enrol.AcademicYearID
        JOIN Teacher teacher ON teacher.TeacherID=offering.TeacherID
        JOIN Credentials cred ON cred.CredentialsID=teacher.CredentialsID
        JOIN School school ON school.SchoolID=teacher.SchoolID
        """;
    private static final String AUTH = """
        WHERE teacher.TeacherID=? AND teacher.CredentialsID=?
          AND cred.Role='Teacher' AND cred.Status='Active' AND cred.MustChangePassword=0
          AND school.Status='Approved' AND st.SchoolID=teacher.SchoolID
          AND subj.SchoolID=teacher.SchoolID AND ay.SchoolID=teacher.SchoolID
          AND offering.AcademicYearID=enrol.AcademicYearID
        """;

    public List<MarkEntry> listForTeacher(int teacherID, int credentialID) throws SQLException {
        String sql = """
            SELECT d.DetailID, CONCAT(st.FName,' ',st.LName) AS StudentName,
                   CONCAT(subj.Code,' - ',subj.Name) AS SubjectName, ay.Name AS YearName,
                   offering.ExamSeriesNumber, offering.PassMark, offering.MaxMark,
                   ay.ExamPeriodOpen, d.IsFrozen, d.FinalMark, d.ResultStatus,
                   e.SeriesNumber, em.Mark
            """ + BASE + """
            LEFT JOIN ExamMark em ON em.DetailID=d.DetailID
            LEFT JOIN Exam e ON e.ExamID=em.ExamID AND e.SubjectAcademicYearID=offering.SubjectAcademicYearID
            """ + AUTH + " ORDER BY ay.Name, subj.Code, st.LName, st.FName, d.DetailID, e.SeriesNumber";
        Map<Integer, MarkEntry> headers = new LinkedHashMap<>();
        Map<Integer, Map<Integer, BigDecimal>> marks = new HashMap<>();
        try (Connection c=connections.open(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setInt(1,teacherID); p.setInt(2,credentialID);
            try (ResultSet r=p.executeQuery()) {
                while(r.next()) {
                    int id=r.getInt("DetailID");
                    if (!headers.containsKey(id)) {
                        headers.put(id,new MarkEntry(id,r.getString("StudentName"),r.getString("SubjectName"),r.getString("YearName"),
                            r.getInt("ExamSeriesNumber"),r.getBigDecimal("PassMark"),r.getBigDecimal("MaxMark"),
                            r.getBoolean("ExamPeriodOpen"),r.getBoolean("IsFrozen"),Map.of(),r.getBigDecimal("FinalMark"),r.getString("ResultStatus")));
                        marks.put(id,new TreeMap<>());
                    }
                    BigDecimal mark=r.getBigDecimal("Mark");
                    int series=r.getInt("SeriesNumber");
                    if(mark!=null && !r.wasNull()) marks.get(id).put(series,mark);
                }
            }
        }
        List<MarkEntry> result=new ArrayList<>();
        for (MarkEntry h:headers.values()) result.add(new MarkEntry(h.detailID(),h.student(),h.subject(),h.academicYear(),h.seriesCount(),h.passMark(),h.maxMark(),h.examOpen(),h.frozen(),marks.get(h.detailID()),h.finalMark(),h.resultStatus()));
        return result;
    }

    public ExamMarkRules.Result saveMark(int teacherID,int credentialID,int detailID,int series,BigDecimal mark) throws SQLException {
        mark=ExamMarkRules.validateMark(mark);
        try(Connection c=connections.open()) {
            c.setAutoCommit(false);
            try {
                int offering, seriesCount; BigDecimal pass,max;
                // Lock the enrolment and its related authorization/configuration rows until commit.
                String lock="SELECT offering.SubjectAcademicYearID, offering.ExamSeriesNumber, offering.PassMark, offering.MaxMark\n"
                        + BASE + AUTH + " AND d.DetailID=? AND ay.ExamPeriodOpen=1 AND d.IsFrozen=0 FOR UPDATE";
                try(PreparedStatement p=c.prepareStatement(lock)) {
                    p.setInt(1,teacherID);p.setInt(2,credentialID);p.setInt(3,detailID);
                    try(ResultSet r=p.executeQuery()) {
                        if(!r.next()) throw new SQLException("Marking is unavailable: check teacher assignment, exam period and frozen status.");
                        offering=r.getInt(1);seriesCount=r.getInt(2);pass=r.getBigDecimal(3);max=r.getBigDecimal(4);
                    }
                }
                if(series<1 || series>seriesCount) throw new IllegalArgumentException("Invalid examination series.");
                Map<Integer,BigDecimal> marks=new HashMap<>();
                try(PreparedStatement p=c.prepareStatement("SELECT e.SubjectAcademicYearID,e.SeriesNumber,em.Mark FROM ExamMark em JOIN Exam e ON e.ExamID=em.ExamID WHERE em.DetailID=? FOR UPDATE")) {
                    p.setInt(1,detailID);
                    try(ResultSet r=p.executeQuery()) {
                        while(r.next()) {
                            if(r.getInt(1)!=offering) throw new SQLException("Existing exam marks refer to another subject offering. Contact your manager.");
                            marks.put(r.getInt(2),r.getBigDecimal(3));
                        }
                    }
                }
                marks.put(series,mark);
                ExamMarkRules.Result result=ExamMarkRules.calculate(marks,seriesCount,pass,max);
                try(PreparedStatement p=c.prepareStatement("INSERT INTO Exam (SubjectAcademicYearID,SeriesNumber) VALUES (?,?) ON DUPLICATE KEY UPDATE ExamID=ExamID")) {
                    p.setInt(1,offering);p.setInt(2,series);p.executeUpdate();
                }
                int examID;
                try(PreparedStatement p=c.prepareStatement("SELECT ExamID FROM Exam WHERE SubjectAcademicYearID=? AND SeriesNumber=?")) {
                    p.setInt(1,offering);p.setInt(2,series);
                    try(ResultSet r=p.executeQuery()) { if(!r.next()) throw new SQLException("Examination series not found.");examID=r.getInt(1); }
                }
                try(PreparedStatement p=c.prepareStatement("INSERT INTO ExamMark (DetailID,ExamID,Mark) VALUES (?,?,?) ON DUPLICATE KEY UPDATE Mark=?")) {
                    p.setInt(1,detailID);p.setInt(2,examID);p.setBigDecimal(3,mark);p.setBigDecimal(4,mark);p.executeUpdate();
                }
                try(PreparedStatement p=c.prepareStatement("UPDATE Details SET FinalMark=?,ResultStatus=? WHERE DetailID=? AND IsFrozen=0")) {
                    if(result.finalMark()==null) p.setNull(1,Types.DECIMAL); else p.setBigDecimal(1,result.finalMark());
                    p.setString(2,result.status());p.setInt(3,detailID);p.executeUpdate();
                }
                c.commit();return result;
            } catch(SQLException | RuntimeException | Error ex) {
                try { c.rollback(); } catch(SQLException rollback) { ex.addSuppressed(rollback); }
                throw ex;
            }
        }
    }
}
