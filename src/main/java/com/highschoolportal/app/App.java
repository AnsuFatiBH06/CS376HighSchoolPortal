package com.highschoolportal.app;
import java.awt.CardLayout;
import java.util.Stack;

import javax.swing.JFrame;
import javax.swing.JPanel;

// import com.highschoolportal.ui.LoginUI;
import com.highschoolportal.ui.ManagerHomePageUI;


public class App {
    private JFrame mainFrame;
    private CardLayout cardLayout;
    private JPanel innerContainer;
    private Stack<String> pageHistory;

    public App() {
        this.pageHistory = new Stack<>();
        this.mainFrame = new JFrame("High School Portal");

        this.mainFrame.setSize(1000, 700);
        this.mainFrame.setLocationRelativeTo(null);
        this.mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.cardLayout = new CardLayout();
        this.innerContainer = new JPanel(cardLayout);

        // innerContainer.add(new LoginUI(this), "LOGIN");

        this.innerContainer.add(new ManagerHomePageUI(this), "MANAGER_HOMEPAGE");
        this.mainFrame.add(this.innerContainer);

        // showPage("LOGIN");

        this.mainFrame.setVisible(true);
    }

    public void showPage(String pageName) {
        this.cardLayout.show(this.innerContainer, pageName);
        this.pageHistory.push(pageName);
    }

    public void goBack() {
        if(this.pageHistory.size() <= 1) {
            return;
        }
        this.pageHistory.pop();

        String previousPage =
                this.pageHistory.peek();

        this.cardLayout.show(this.innerContainer, previousPage);
    }

    public static void main(String[] args) {
        new App();
    }
}
