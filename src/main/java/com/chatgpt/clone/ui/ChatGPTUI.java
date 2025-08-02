package com.chatgpt.clone.ui;

import com.chatgpt.clone.exception.OpenAIException;
import com.chatgpt.clone.model.Message;
import com.chatgpt.clone.service.GPTService;
import com.chatgpt.clone.util.Logger;
import com.chatgpt.clone.util.OpenAIUtil;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A simple Swing-based UI for interacting with the GPT service.
 */
public class ChatGPTUI extends JFrame {
    private static final Logger logger = new Logger(ChatGPTUI.class);
    
    private final GPTService gptService;
    private final List<Message> conversation;
    private final VoiceManager voiceManager;
    
    private JTextPane chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private JButton clearButton;
    private JComboBox<String> modelSelector;
    private JCheckBox systemMessageCheckbox;
    private JTextField systemMessageField;
    private JCheckBox voiceEnabledCheckbox;
    
    private final AtomicBoolean isSpeaking = new AtomicBoolean(false);
    
    private final String[] AVAILABLE_MODELS = {"gpt-3.5-turbo", "gpt-4"};
    
    /**
     * Creates a new ChatGPTUI instance.
     */
    public ChatGPTUI() {
        super("ChatGPT Clone");
        
        // Initialize the GPT service
        gptService = new GPTService();
        
        // Initialize the conversation
        conversation = new ArrayList<>();
        
        // Initialize the voice manager
        voiceManager = new VoiceManager();
        
        // Set up the UI components
        initializeUI();
        
        // Set up event handlers
        setupEventHandlers();
        
        // Set default system message
        systemMessageField.setText("You are a helpful assistant.");
        
        // Add initial system message to conversation
        updateSystemMessage();
        
        logger.info("ChatGPT UI initialized");
    }
    
    /**
     * Initializes the UI components.
     */
    private void initializeUI() {
        // Set window properties
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Create components
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setContentType("text/html");
        
        inputField = new JTextField();
        sendButton = new JButton("Send");
        clearButton = new JButton("Clear Chat");
        modelSelector = new JComboBox<>(AVAILABLE_MODELS);
        systemMessageCheckbox = new JCheckBox("System Message:", true);
        systemMessageField = new JTextField();
        voiceEnabledCheckbox = new JCheckBox("Enable Voice", voiceManager.isEnabled());
        
        // Layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Chat area with scroll pane
        JScrollPane scrollPane = new JScrollPane(chatArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        // Control panel
        JPanel controlPanel = new JPanel(new BorderLayout());
        
        // Model selection panel
        JPanel modelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        modelPanel.add(new JLabel("Model:"));
        modelPanel.add(modelSelector);
        modelPanel.add(clearButton);
        modelPanel.add(voiceEnabledCheckbox);
        
        // System message panel
        JPanel systemPanel = new JPanel(new BorderLayout());
        systemPanel.add(systemMessageCheckbox, BorderLayout.WEST);
        systemPanel.add(systemMessageField, BorderLayout.CENTER);
        
        controlPanel.add(modelPanel, BorderLayout.NORTH);
        controlPanel.add(systemPanel, BorderLayout.SOUTH);
        
        // Bottom panel combining control and input
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(controlPanel, BorderLayout.NORTH);
        bottomPanel.add(inputPanel, BorderLayout.SOUTH);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        // Add padding
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Set the content pane
        setContentPane(mainPanel);
    }
    
    /**
     * Sets up event handlers for UI components.
     */
    private void setupEventHandlers() {
        // Send button action
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        
        // Enter key in input field
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        
        // Clear button action
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearChat();
            }
        });
        
        // System message checkbox
        systemMessageCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                systemMessageField.setEnabled(systemMessageCheckbox.isSelected());
                updateSystemMessage();
            }
        });
        
        // Window close event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                logger.info("ChatGPT UI closing");
                voiceManager.cleanup();
            }
        });
        
        // Voice checkbox
        voiceEnabledCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                voiceManager.setEnabled(voiceEnabledCheckbox.isSelected());
                logger.info("Voice " + (voiceEnabledCheckbox.isSelected() ? "enabled" : "disabled"));
            }
        });
    }
    
    /**
     * Sends a message to the GPT service and displays the response.
     */
    private void sendMessage() {
        String userInput = inputField.getText().trim();
        if (userInput.isEmpty()) {
            return;
        }
        
        // Clear the input field
        inputField.setText("");
        
        // Update system message if needed
        if (systemMessageCheckbox.isSelected() && 
                !systemMessageField.getText().trim().isEmpty()) {
            updateSystemMessage();
        }
        
        // Add user message to conversation
        OpenAIUtil.addUserMessage(conversation, userInput);
        
        // Display user message
        appendToChat("You", userInput);
        
        // Disable input while waiting for response
        setInputEnabled(false);
        
        // Get selected model
        String selectedModel = (String) modelSelector.getSelectedItem();
        
        // Create a worker thread to prevent UI freezing
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                try {
                    logger.info("Sending message to GPT model: " + selectedModel);
                    return gptService.sendConversation(conversation, selectedModel);
                } catch (OpenAIException e) {
                    logger.error("Error sending message to GPT", e);
                    return "Error: " + OpenAIUtil.formatError(e);
                }
            }
            
            @Override
            protected void done() {
                try {
                    String response = get();
                    
                    // Check if it's an error message
                    if (response.startsWith("Error: ")) {
                        appendToChat("System", response, Color.RED);
                    } else {
                        // Add assistant message to conversation
                        OpenAIUtil.addAssistantMessage(conversation, response);
                        
                        // Display assistant message
                        appendToChat("ChatGPT", response);
                        
                        // Speak the response if voice is enabled
                        if (voiceManager.isEnabled() && !isSpeaking.get()) {
                            isSpeaking.set(true);
                            new Thread(() -> {
                                try {
                                    voiceManager.speak(response);
                                } finally {
                                    isSpeaking.set(false);
                                }
                            }).start();
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error processing GPT response", e);
                    appendToChat("System", "Error: " + e.getMessage(), Color.RED);
                } finally {
                    // Re-enable input
                    setInputEnabled(true);
                    inputField.requestFocus();
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Appends a message to the chat area.
     * 
     * @param sender The sender of the message
     * @param message The message content
     */
    private void appendToChat(String sender, String message) {
        appendToChat(sender, message, null);
    }
    
    /**
     * Appends a message to the chat area with a specific color.
     * 
     * @param sender The sender of the message
     * @param message The message content
     * @param color The color for the message, or null for default
     */
    private void appendToChat(String sender, String message, Color color) {
        // Format the message as HTML
        String colorAttr = color != null ? 
                String.format("color:rgb(%d,%d,%d)", color.getRed(), color.getGreen(), color.getBlue()) : 
                "";
        
        String formattedSender = sender.equals("ChatGPT") ? 
                String.format("<b style='%s'>%s:</b> ", colorAttr, sender) : 
                String.format("<b style='%s'>%s:</b> ", colorAttr, sender);
        
        // Replace newlines with HTML breaks
        String formattedMessage = message.replace("\n", "<br>");
        
        // Append to the HTML document
        HTMLDocument doc = (HTMLDocument) chatArea.getDocument();
        HTMLEditorKit editorKit = (HTMLEditorKit) chatArea.getEditorKit();
        
        try {
            editorKit.insertHTML(doc, doc.getLength(), 
                    "<p>" + formattedSender + formattedMessage + "</p>", 
                    0, 0, null);
            
            // Scroll to the bottom
            chatArea.setCaretPosition(doc.getLength());
        } catch (Exception e) {
            logger.error("Error appending to chat", e);
        }
    }
    
    /**
     * Clears the chat area and conversation history.
     */
    private void clearChat() {
        // Clear the chat area
        chatArea.setText("");
        
        // Clear the conversation, but keep the system message if enabled
        conversation.clear();
        updateSystemMessage();
        
        logger.info("Chat cleared");
    }
    
    /**
     * Updates the system message in the conversation.
     */
    private void updateSystemMessage() {
        // Remove any existing system messages
        conversation.removeIf(message -> "system".equals(message.getRole()));
        
        // Add system message if enabled
        if (systemMessageCheckbox.isSelected()) {
            String systemMessage = systemMessageField.getText().trim();
            if (!systemMessage.isEmpty()) {
                conversation.add(0, Message.systemMessage(systemMessage));
                logger.debug("System message updated: " + systemMessage);
            }
        }
    }
    
    /**
     * Enables or disables input components.
     * 
     * @param enabled Whether the input components should be enabled
     */
    private void setInputEnabled(boolean enabled) {
        inputField.setEnabled(enabled);
        sendButton.setEnabled(enabled);
        clearButton.setEnabled(enabled);
        modelSelector.setEnabled(enabled);
        systemMessageCheckbox.setEnabled(enabled);
        systemMessageField.setEnabled(enabled && systemMessageCheckbox.isSelected());
        voiceEnabledCheckbox.setEnabled(enabled);
    }
    
    /**
     * Main method to start the application.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Set the look and feel to the system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            logger.error("Error setting look and feel", e);
        }
        
        // Create and show the UI on the EDT
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ChatGPTUI ui = new ChatGPTUI();
                ui.setVisible(true);
                logger.info("ChatGPT UI started");
            }
        });
    }
}