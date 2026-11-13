package com.highschoolportal.app;

import java.awt.CardLayout;
import java.util.Stack;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.highschoolportal.model.Manager;
import com.highschoolportal.ui.PageNames;
import com.highschoolportal.ui.managerpages.*;

public class App {

    private final JFrame mainFrame;
    private final CardLayout cardLayout;
    private final JPanel innerContainer;
    private final Stack<String> pageHistory;
    private final Manager currentManager;

    public App() {
        this(null);
    }

    public App(Manager currentManager) {

        this.currentManager = currentManager;

        pageHistory = new Stack<>();

        mainFrame = new JFrame("High School Portal");

        mainFrame.setSize(1000, 700);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        innerContainer = new JPanel(cardLayout);

        innerContainer.add(
                new ManagerHomePageUI(this),
                PageNames.MANAGER_HOMEPAGE
        );

        innerContainer.add(
                new SchoolSignupUI(this),
                PageNames.SCHOOLSIGNUPPAGE
        );

        innerContainer.add(
                new BusinessRuleUI(this),
                PageNames.ACADEMIC_YEAR_MANAGEMENT
        );

        mainFrame.add(innerContainer);

        showPage(PageNames.MANAGER_HOMEPAGE);

        mainFrame.setVisible(true);
    }

    public Manager getCurrentManager() {
        return currentManager;
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

    public static void main(String[] args) {

        new App();
    }
}