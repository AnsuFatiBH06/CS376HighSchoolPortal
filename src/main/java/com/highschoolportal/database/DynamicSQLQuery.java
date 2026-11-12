package com.highschoolportal.database;
import com.highschoolportal.model.School;

public class DynamicSQLQuery {
    private String selectStatement;
    private String fromClause;
    private String whereClause;
    private String insertStatement;
    private String valuesClause;

    private DynamicSQLQuery() {}
    
    private static class Holder {
        private static final DynamicSQLQuery INSTANCE = new DynamicSQLQuery();
    }

    public static DynamicSQLQuery getInstance() {
        return Holder.INSTANCE;
    }

    public boolean insertSchool(School school) {
        this.insertStatement = "INSERT INTO SCHOOL";
        this.valuesClause = "VALUES("
                                + ", " + school.getSchoolEmail()
                                + ", " + school.getSchoolEmailDomain()
                                + ", " + school.getSchoolAddress()
                                + ", " + school.getSchoolStatus()
                                + ")";
        // need a way to automatically set the id in the database since its missing here

        String finalStatement = this.insertStatement + " " + this.valuesClause + ";";

        // Use finalStatement in the database
        // If successfully added, return true
        // Else return false

        return false;
    }
}
