package com.highschoolportal.app;

import java.awt.CardLayout;
import java.util.Stack;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.highschoolportal.model.User;

import com.highschoolportal.ui.PageNames;
import com.highschoolportal.ui.adminpages.*;
import com.highschoolportal.ui.managerpages.*;
import com.highschoolportal.ui.studentpages.*;
import com.highschoolportal.ui.teacherpages.*;

public class App {

    private final JFrame mainFrame;
    private final CardLayout cardLayout;
    private final JPanel innerContainer;
    private final Stack<String> pageHistory;
    private User currentUser;

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
        showPage(PageNames.MANAGER_HOMEPAGE);

        // keep this segment
        mainFrame.setVisible(true);
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        currentUser = user;
    }

    public void showPage(String pageName) {

        cardLayout.show(innerContainer, pageName);

        pageHistory.push(pageName);
    }

    public void goBack() {
        if (pageHistory.size() <= 1) {
            return;
        }

        pageHistory.pop();
        String previousPage = pageHistory.peek();
        
        cardLayout.show(innerContainer, previousPage);
    }

    private void addPages() {
        // Admin Pages
        innerContainer.add(new SystemAdminHomePageUI(this), PageNames.ADMIN_HOMEPAGE);
        innerContainer.add(new ApprovedSchoolsUI(this), PageNames.ADMIN_APPROVED_SCHOOLS);
        innerContainer.add(new PendingSchoolsUI(this), PageNames.ADMIN_PENDING_SCHOOLS);

        // Manager Pages
        innerContainer.add(new ManagerHomePageUI(this), PageNames.MANAGER_HOMEPAGE);
        innerContainer.add(new SchoolSignupUI(this), PageNames.SCHOOLSIGNUPPAGE);
        innerContainer.add(new BusinessRuleUI(this), PageNames.ACADEMIC_YEAR_MANAGEMENT);
    }

    public static void main(String[] args) {
        new App();
    }
}