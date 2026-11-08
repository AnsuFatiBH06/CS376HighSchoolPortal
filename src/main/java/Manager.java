public class Manager extends User {
    private int schoolID;

    public Manager(int userID, String fName, String lName, String email, int credentialsID) {
        super(userID, fName, lName, email, credentialsID);
        this.schoolID = -1;
    } // still doesnt belong to a school

    public Manager(int userID, String fName, String lName, String email, int credentialsID, int schoolID) {
        super(userID, fName, lName, email, credentialsID);
        this.schoolID = schoolID;
    } // has a school

    public int getSchoolID() {
        return this.schoolID;
    }

    public void registerSchool() {

    }
}
