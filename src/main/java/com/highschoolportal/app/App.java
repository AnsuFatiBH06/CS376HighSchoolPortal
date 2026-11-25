package com.highschoolportal.app;

import java.awt.CardLayout;
import java.util.Stack;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import com.highschoolportal.model.Credentials;
import com.highschoolportal.model.User;
import com.highschoolportal.services.AuthenticationService;
import com.highschoolportal.services.RememberMeCoordinator;
import com.highschoolportal.ui.PageNames;
import com.highschoolportal.ui.adminpages.ApprovedSchoolsUI;
import com.highschoolportal.ui.adminpages.PendingSchoolsUI;
import com.highschoolportal.ui.adminpages.SystemAdminHomePageUI;
import com.highschoolportal.ui.managerpages.BusinessRuleUI;
import com.highschoolportal.ui.managerpages.ManagerHomePageUI;
import com.highschoolportal.ui.managerpages.ManagerPasswordManagementUI;
import com.highschoolportal.ui.managerpages.ManagerSubjectsUI;
import com.highschoolportal.ui.managerpages.ManagerTeacherUI;
import com.highschoolportal.ui.managerpages.SchoolSignupUI;
import com.highschoolportal.ui.shared.LoginUI;
import com.highschoolportal.ui.shared.PasswordResetRequestUI;
import com.highschoolportal.ui.shared.UpdatePasswordUI;
import com.highschoolportal.ui.studentpages.StudentAttendanceUI;
import com.highschoolportal.ui.studentpages.StudentHomePageUI;
import com.highschoolportal.ui.teacherpages.TeacherAttendanceUI;
import com.highschoolportal.ui.teacherpages.TeacherHomePageUI;

public class App {

    private final JFrame mainFrame;
    private final CardLayout cardLayout;
    private final JPanel innerContainer;
    private final Stack<String> pageHistory;
    private User currentUser;
    private Credentials.Role currentRole;
    private boolean passwordChangeRequired;
    private final RememberMeCoordinator rememberMe
            = new RememberMeCoordinator();

    // public App() {
    //     this(null);
    // }
    public App() {

        pageHistory = new Stack<>();

        mainFrame = new JFrame("High School Portal");

        mainFrame.setSize(1000, 700);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        innerContainer = new JPanel(cardLayout);

        addPages();

        mainFrame.add(innerContainer);

        // this code segment should be implemented in LoginUI, but right now we're testing things out
        // In LoginUI, we need to know the user role so that we create the correct object and open the right homepage.
        // LoginUI could/maybe should use setCurrentUser() method ======> app.setCurrentUser(new Manager(.....))
        showPage(PageNames.LOGIN);

        // keep this segment
        mainFrame.setVisible(true);
        restoreRememberedLogin();
    }

    public JFrame getCurrentFrame() {
        return mainFrame;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        currentUser = user;
    }

    public void showPage(String pageName) {

        if (currentUser == null) {
            boolean publicPage
                    = pageName.equals(PageNames.LOGIN)
                    || pageName.equals(PageNames.MANAGER_SCHOOL_SIGNUP)
                    || pageName.equals(PageNames.PASSWORD_RESET_REQUEST);

            if (!publicPage) {
                pageName = PageNames.LOGIN;
            }
        } else if (passwordChangeRequired) {
            pageName = PageNames.UPDATE_PASSWORD;
        }

        if (currentUser != null && !passwordChangeRequired) {

            boolean allowed = switch (pageName) {
                case PageNames.ADMIN_HOMEPAGE ->
                    currentRole == Credentials.Role.Admin;

                case PageNames.MANAGER_HOMEPAGE ->
                    currentRole == Credentials.Role.Manager;

                case PageNames.TEACHER_HOMEPAGE,
                PageNames.TEACHER_ATTENDANCE  ->
                    currentRole == Credentials.Role.Teacher;

                case PageNames.STUDENT_HOMEPAGE, PageNames.STUDENT_ATTENDANCE ->
                    currentRole == Credentials.Role.Student;

                default ->
                    true;
            };

            if (!allowed) {
                return;
            }
        }

        cardLayout.show(innerContainer, pageName);
        pageHistory.push(pageName);
    }

    public void goBack() {
        if (pageHistory.size() <= 1) {
            return;
        }

        pageHistory.pop();
        String previousPage = pageHistory.pop();

        showPage(previousPage);
    }

    private void addPages() {
        innerContainer.add(new LoginUI(this), PageNames.LOGIN);
        // Admin Pages
        innerContainer.add(new SystemAdminHomePageUI(this), PageNames.ADMIN_HOMEPAGE);
        innerContainer.add(new ApprovedSchoolsUI(this), PageNames.ADMIN_APPROVED_SCHOOLS);
        innerContainer.add(new PendingSchoolsUI(this), PageNames.ADMIN_PENDING_SCHOOLS);

        // Manager Pages
        innerContainer.add(new ManagerHomePageUI(this), PageNames.MANAGER_HOMEPAGE);
        innerContainer.add(new SchoolSignupUI(this), PageNames.MANAGER_SCHOOL_SIGNUP);
        innerContainer.add(new BusinessRuleUI(this), PageNames.MANAGER_ACADEMIC_YEAR_MANAGEMENT);
        innerContainer.add(new ManagerSubjectsUI(this), PageNames.MANAGER_SUBJECT_MANAGEMENT);
        innerContainer.add(new ManagerPasswordManagementUI(this), PageNames.MANAGER_PASSWORD_MANAGEMENT);
        innerContainer.add(new ManagerTeacherUI(this), PageNames.MANAGER_TEACHER_MANAGEMENT);

        // Teacher and Student password pages
        innerContainer.add(new PasswordResetRequestUI(), PageNames.PASSWORD_RESET_REQUEST);
        innerContainer.add(new UpdatePasswordUI(this), PageNames.UPDATE_PASSWORD);
        innerContainer.add(new TeacherHomePageUI(this), PageNames.TEACHER_HOMEPAGE);
        innerContainer.add(new StudentHomePageUI(this), PageNames.STUDENT_HOMEPAGE);
        innerContainer.add(new StudentAttendanceUI(this), PageNames.STUDENT_ATTENDANCE);
        innerContainer.add(new TeacherAttendanceUI(this), PageNames.TEACHER_ATTENDANCE);
    }

    public void completeLogin(
            AuthenticationService.LoginResult result) {

        currentUser = result.user();
        currentRole = result.role();
        passwordChangeRequired = result.mustChangePassword();

        pageHistory.clear();

        if (passwordChangeRequired) {
            showPage(PageNames.UPDATE_PASSWORD);
        } else {
            openHomePage();
        }
    }

    private void openHomePage() {
        if (currentUser == null || currentRole == null) {
            showPage(PageNames.LOGIN);
            return;
        }

        switch (currentRole) {
            case Admin ->
                showPage(PageNames.ADMIN_HOMEPAGE);
            case Manager ->
                showPage(PageNames.MANAGER_HOMEPAGE);
            case Teacher ->
                showPage(PageNames.TEACHER_HOMEPAGE);
            case Student ->
                showPage(PageNames.STUDENT_HOMEPAGE);
        }
    }

    public void passwordChangeCompleted() {
        if (currentUser == null || !passwordChangeRequired) {
            return;
        }

        passwordChangeRequired = false;
        pageHistory.clear();
        openHomePage();
    }

    public void logout() {
        currentUser = null;
        currentRole = null;
        passwordChangeRequired = false;

        pageHistory.clear();
        showPage(PageNames.LOGIN);

        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                rememberMe.forget();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                            mainFrame,
                            "Logged out locally, but session revocation "
                            + "could not be confirmed.",
                            "Logout Warning",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            }
        }.execute();

    }

    private void restoreRememberedLogin() {

        new SwingWorker<
            java.util.Optional<AuthenticationService.LoginResult>, Void>() {

            @Override
            protected java.util.Optional<
                AuthenticationService.LoginResult>
                    doInBackground() throws Exception {

                return rememberMe.restore();
            }

            @Override
            protected void done() {
                try {
                    var result = get();

                    if (result.isPresent() && currentUser == null) {
                        completeLogin(result.get());
                    }

                } catch (Exception e) {
                    System.err.println(
                            "Remember Me restoration failed: "
                            + e.getMessage()
                    );
                }
            }
        }.execute();
    }

    public static void main(String[] args) {
        new App();
    }
}
