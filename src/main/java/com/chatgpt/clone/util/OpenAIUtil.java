package com.chatgpt.clone.util;

import com.chatgpt.clone.exception.OpenAIException;
import com.chatgpt.clone.model.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for OpenAI API related operations.
 */
public class OpenAIUtil {

    /**
     * Creates a new conversation with a system message.
     * 
     * @param systemMessage The system message to set the context
     * @return A new list of messages with the system message
     */
    public static List<Message> createConversation(String systemMessage) {
        List<Message> conversation = new ArrayList<>();
        conversation.add(Message.systemMessage(systemMessage));
        return conversation;
    }

    /**
     * Adds a user message to the conversation and returns the updated conversation.
     * 
     * @param conversation The existing conversation
     * @param userMessage The user message to add
     * @return The updated conversation
     */
    public static List<Message> addUserMessage(List<Message> conversation, String userMessage) {
        conversation.add(Message.userMessage(userMessage));
        return conversation;
    }

    /**
     * Adds an assistant message to the conversation and returns the updated conversation.
     * 
     * @param conversation The existing conversation
     * @param assistantMessage The assistant message to add
     * @return The updated conversation
     */
    public static List<Message> addAssistantMessage(List<Message> conversation, String assistantMessage) {
        Message message = Message.builder()
                .role("assistant")
                .content(assistantMessage)
                .build();
        conversation.add(message);
        return conversation;
    }

    /**
     * Formats an OpenAI exception message for display.
     * 
     * @param e The OpenAIException
     * @return A formatted error message
     */
    public static String formatError(OpenAIException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("OpenAI API Error: ").append(e.getMessage());
        
        if (e.getStatusCode() > 0) {
            sb.append("\nStatus Code: ").append(e.getStatusCode());
            sb.append("\nError Type: ").append(e.getErrorType());
        }
        
        return sb.toString();
    }
}