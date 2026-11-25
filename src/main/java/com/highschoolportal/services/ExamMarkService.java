package com.highschoolportal.services;

import com.highschoolportal.model.ExamMarkRules;
import com.highschoolportal.model.User;
import com.highschoolportal.repository.ExamMarkRepository;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class ExamMarkService {
    private final ExamMarkRepository repository;
    public ExamMarkService() { this(new ExamMarkRepository()); }
    public ExamMarkService(ExamMarkRepository repository) { this.repository=Objects.requireNonNull(repository); }
    private void validateUser(User user) {
        if(user==null || user.getUserID()<1 || user.getCredentialsID()<1) throw new IllegalArgumentException("A valid teacher login is required.");
    }
    public List<ExamMarkRepository.MarkEntry> listForTeacher(User user) throws SQLException {
        validateUser(user);
        return repository.listForTeacher(user.getUserID(),user.getCredentialsID());
    }
    public ExamMarkRules.Result saveMark(User user,int detailID,int series,String text) throws SQLException {
        validateUser(user);
        if(detailID<1 || series<1) throw new IllegalArgumentException("Select a student and examination series.");
        BigDecimal mark;
        try { mark=new BigDecimal(text==null?"":text.trim()); }
        catch(NumberFormatException ex) { throw new IllegalArgumentException("Enter a valid numeric mark.",ex); }
        return repository.saveMark(user.getUserID(),user.getCredentialsID(),detailID,series,ExamMarkRules.validateMark(mark));
    }
}
