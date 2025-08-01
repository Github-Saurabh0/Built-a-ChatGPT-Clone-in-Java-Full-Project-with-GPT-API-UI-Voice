package com.chatgpt.clone.example;

import com.chatgpt.clone.exception.OpenAIException;
import com.chatgpt.clone.model.Message;
import com.chatgpt.clone.service.GPTService;
import com.chatgpt.clone.util.Logger;
import com.chatgpt.clone.util.OpenAIUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Example class demonstrating how to use the GPTService.
 */
public class GPTExample {
    private static final Logger logger = new Logger(GPTExample.class);

    public static void main(String[] args) {
        logger.info("Starting GPT Example application");
        
        // Create a new GPTService instance
        GPTService gptService = new GPTService();
        
        // Simple question-answer example
        try {
            System.out.println("=== Simple Question-Answer Example ===");
            String question = "What is the capital of France?";
            System.out.println("Question: " + question);
            
            logger.info("Sending simple question to GPT");
            String answer = gptService.askQuestion(question);
            System.out.println("Answer: " + answer);
            System.out.println();
            
            // Interactive conversation example
            runInteractiveConversation(gptService);
            
        } catch (OpenAIException e) {
            String errorMessage = OpenAIUtil.formatError(e);
            System.err.println(errorMessage);
            logger.error("OpenAI API Error", e);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            logger.error("Unexpected error", e);
        }
        
        logger.info("GPT Example application finished");
    }
    
    /**
     * Runs an interactive conversation with the GPT model.
     * 
     * @param gptService The GPTService to use
     */
    private static void runInteractiveConversation(GPTService gptService) {
        logger.info("Starting interactive conversation");
        
        Scanner scanner = new Scanner(System.in);
        List<Message> conversation = new ArrayList<>();
        
        // Add a system message to set the context
        conversation.add(Message.systemMessage("You are a helpful assistant."));
        
        System.out.println("=== Interactive Conversation Example ===");
        System.out.println("Type your messages (type 'exit' to quit):");
        
        while (true) {
            System.out.print("\nYou: ");
            String userInput = scanner.nextLine().trim();
            
            if (userInput.equalsIgnoreCase("exit")) {
                logger.info("User exited interactive conversation");
                break;
            }
            
            // Add the user's message to the conversation
            OpenAIUtil.addUserMessage(conversation, userInput);
            
            // Send the conversation to the GPT model
            logger.info("Sending conversation to GPT");
            String response = gptService.sendConversation(conversation);
            
            // Add the assistant's response to the conversation
            OpenAIUtil.addAssistantMessage(conversation, response);
            
            System.out.println("Assistant: " + response);
        }
        
        scanner.close();
        logger.info("Interactive conversation ended");
    }
}