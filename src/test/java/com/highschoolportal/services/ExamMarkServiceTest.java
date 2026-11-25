package com.highschoolportal.services;

import com.highschoolportal.model.User;
import com.highschoolportal.model.ExamMarkRules;
import com.highschoolportal.repository.ExamMarkRepository;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ExamMarkServiceTest {
    private static BigDecimal n(String value){return new BigDecimal(value);}
    private static final User TEACHER=new User(7,"Test","Teacher","test@example.invalid",42);
    private static final BigDecimal PASS=n("50.00"),MAX=n("100.00");
    private static class FakeRepository extends ExamMarkRepository {
        int teacher,credential,detail,series,calls;
        BigDecimal mark;
        boolean fail;
        FakeRepository(){super(()->{throw new AssertionError("No live database in unit tests");});}
        @Override public List<MarkEntry> listForTeacher(int t,int c)throws SQLException {teacher=t;credential=c;calls++;if(fail)throw new SQLException("database unavailable");return List.of();}
        @Override public ExamMarkRules.Result saveMark(int t,int c,int d,int s,BigDecimal m)throws SQLException {teacher=t;credential=c;detail=d;series=s;mark=m;calls++;if(fail)throw new SQLException("database unavailable");return ExamMarkRules.calculate(Map.of(s,m),2,PASS,MAX);}
    }
    @Test void passesTeacherAndCredentialIDs()throws SQLException {FakeRepository r=new FakeRepository();assertTrue(new ExamMarkService(r).listForTeacher(TEACHER).isEmpty());assertEquals(7,r.teacher);assertEquals(42,r.credential);}
    @Test void savesValidatedDecimal()throws SQLException {FakeRepository r=new FakeRepository();new ExamMarkService(r).saveMark(TEACHER,11,2," 25.5 ");assertEquals(7,r.teacher);assertEquals(42,r.credential);assertEquals(11,r.detail);assertEquals(2,r.series);assertEquals(n("25.50"),r.mark);}
    @Test void rejectsInvalidLogin(){FakeRepository r=new FakeRepository();ExamMarkService s=new ExamMarkService(r);assertThrows(IllegalArgumentException.class,()->s.listForTeacher(null));assertThrows(IllegalArgumentException.class,()->s.saveMark(new User(7,"A","B","a",0),1,1,"10"));assertEquals(0,r.calls);}
    @Test void rejectsInvalidIDs(){FakeRepository r=new FakeRepository();ExamMarkService s=new ExamMarkService(r);assertThrows(IllegalArgumentException.class,()->s.saveMark(TEACHER,0,1,"10"));assertThrows(IllegalArgumentException.class,()->s.saveMark(TEACHER,1,0,"10"));assertEquals(0,r.calls);}
    @Test void rejectsInvalidMarks(){FakeRepository r=new FakeRepository();ExamMarkService s=new ExamMarkService(r);for(String text:new String[]{"","abc","-1","1.234","1000000","NaN"})assertThrows(IllegalArgumentException.class,()->s.saveMark(TEACHER,1,1,text));assertThrows(IllegalArgumentException.class,()->s.saveMark(TEACHER,1,1,null));assertEquals(0,r.calls);}
    @Test void propagatesDatabaseFailure(){FakeRepository r=new FakeRepository();r.fail=true;ExamMarkService s=new ExamMarkService(r);assertThrows(SQLException.class,()->s.listForTeacher(TEACHER));assertThrows(SQLException.class,()->s.saveMark(TEACHER,1,1,"10"));}
    @Test void incompleteUntilAllSeriesEntered(){var result=ExamMarkRules.calculate(Map.of(1,n("60")),2,PASS,MAX);assertEquals(n("60.00"),result.total());assertNull(result.finalMark());assertEquals("Incomplete",result.status());}
    @Test void sumsMarksAndPassesAtThreshold(){var result=ExamMarkRules.calculate(Map.of(1,n("25"),2,n("25")),2,PASS,MAX);assertEquals(n("50.00"),result.finalMark());assertEquals("Passed",result.status());}
    @Test void zeroIsAnEnteredMarkAndCanFail(){var result=ExamMarkRules.calculate(Map.of(1,n("0"),2,n("40")),2,PASS,MAX);assertEquals(n("40.00"),result.finalMark());assertEquals("Failed",result.status());}
    @Test void rejectsCombinedMarksOverMaximum(){assertThrows(IllegalArgumentException.class,()->ExamMarkRules.calculate(Map.of(1,n("60"),2,n("41")),2,PASS,MAX));}
    @Test void rejectsOutOfRangeSeries(){assertThrows(IllegalArgumentException.class,()->ExamMarkRules.calculate(Map.of(3,n("20")),2,PASS,MAX));}
    @Test void rejectsMissingOrInvalidRules(){assertFalse(ExamMarkRules.validConfiguration(0,PASS,MAX));assertFalse(ExamMarkRules.validConfiguration(2,null,MAX));assertFalse(ExamMarkRules.validConfiguration(2,MAX,PASS));assertThrows(IllegalArgumentException.class,()->ExamMarkRules.calculate(Map.of(),0,PASS,MAX));}
}
