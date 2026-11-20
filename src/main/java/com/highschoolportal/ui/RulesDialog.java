package com.highschoolportal.ui;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

public class RulesDialog extends JDialog {
    private JLabel rulesArea;
    private JButton closeButton;

    public RulesDialog(JFrame parent, String windowTitle, String ruleSet) {
        super(parent, windowTitle, true);

        rulesArea = new JLabel();
        rulesArea.setFont(rulesArea.getFont().deriveFont(14f));
        rulesArea.setText(ruleSet);

        add(new JScrollPane(rulesArea), BorderLayout.NORTH);

        closeButton = new JButton("Close");
        add(new JScrollPane(closeButton), BorderLayout.SOUTH);

        closeButton.addActionListener(event -> {
            setVisible(false);
        });

        setSize(600, 350);
        setLocationRelativeTo(parent);
        setVisible(true);
    }
}
