package org.example;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static javax.swing.JFileChooser.DIRECTORIES_ONLY;
import static javax.swing.SwingUtilities.updateComponentTreeUI;

public class GUI {
    static File[] selectedSources;
    static Map<String, JLabel> componentMap;
    static JFrame frame = new JFrame("ffmpeg stuff");

    public static void main(String[] args) {
        componentMap = new HashMap<>();

        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel internalPanel = new JPanel();
        internalPanel.setBounds(5, 30, 575, 380);
        internalPanel.setLayout(new GridLayout(10,2));
        internalPanel.setVisible(true);
        internalPanel.setBackground(new Color(200,200,200));

        JButton executeButton = new JButton("Execute!");
        JButton fileChooserButton = generateFileChooser(internalPanel, executeButton);

        executeButton.setBounds(450, 425, 125, 30);
        executeButton.setEnabled(false);
        executeButton.addActionListener(e -> {
            executeButton.setEnabled(false);
            for (File source : selectedSources) {
                App.execute(source.getAbsolutePath());
            }
        });

        frame.add(fileChooserButton);
        frame.add(executeButton);
        frame.add(internalPanel);
        frame.setSize(600, 500);
        frame.setLayout(null);
        frame.setVisible(true);
    }

    private static JButton generateFileChooser(JPanel internalPanel, JButton executeButton) {
        JButton fileChooserButton = new JButton("Choose directory with files");
        fileChooserButton.setBounds(5, 5, 200, 20);
        fileChooserButton.addActionListener(e -> {
            selectedSources = null;
            componentMap.clear();
            internalPanel.removeAll();

            JFileChooser fileChooser = new JFileChooser(new File("C:/"));
            fileChooser.setMultiSelectionEnabled(true);
            fileChooser.setFileSelectionMode(DIRECTORIES_ONLY);
            fileChooser.showOpenDialog(frame);
            selectedSources = fileChooser.getSelectedFile().listFiles();
            executeButton.setEnabled(true);
            int startHeight = 35;
            assert selectedSources != null;
            for (int i = 0; i < selectedSources.length; i++) {
                File selectedFile = selectedSources[i];

                JPanel row = new JPanel();
                JLabel fileName = new JLabel(selectedFile.getName());
                JLabel completionStatus = new JLabel("0%");

                row.setLayout(new GridLayout(1, 2));
                row.setBounds(10, 20*i + startHeight, 570, 10);
                row.setVisible(true);

                fileName.setBounds(15, 5, 200, 20);
                completionStatus.setBounds(250, 5, 100, 20);
                completionStatus.setName(selectedFile.getName());

                row.add(fileName);
                row.add(completionStatus);
                componentMap.put(selectedFile.getName(), completionStatus);
                internalPanel.add(row);
                updateComponentTreeUI(frame);
            }
        });
        return fileChooserButton;
    }
}
