package com.chatgpt.clone.ui;

import com.chatgpt.clone.util.Logger;

import javax.swing.*;

/**
 * Main application class that launches the ChatGPT UI.
 */
public class ChatGPTApp {
    private static final Logger logger = new Logger(ChatGPTApp.class);
    
    public static void main(String[] args) {
        try {
            // Set the look and feel to the system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            logger.error("Could not set system look and feel", e);
        }
        
        // Launch the UI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            logger.info("Starting ChatGPT UI application");
            ChatGPTUI ui = new ChatGPTUI();
            ui.setVisible(true);
        });
    }
}