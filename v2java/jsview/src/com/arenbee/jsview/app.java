package com.arenbee.jsview;

import javax.swing.*;

public class app {
    private JPanel panelMain;
    private JTabbedPane leftTab;
    private JPanel rightPanel;
    private JPanel explorerPanel;
    private JPanel searchPanel;
    private JTree explorerTree;

    public app() {

        JFrame mainFrame = new JFrame("app");
        mainFrame.setContentPane(panelMain);

        String top = "/media/veracrypt1/stuff";
        String value = System.getenv("OS");
        if (value != null && value.equals("Windows_NT"))
        {
            top = "C:/";
        }
        explorerTree.setModel(new FilesContentProvider(top));

        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.pack();
        mainFrame.setVisible(true);


    }

    public static void main(String[] args) {

        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        new app();



    }
}
