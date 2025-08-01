package com.chatgpt.clone.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a message in the OpenAI chat completion API.
 * Each message has a role (system, user, or assistant) and content.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    /**
     * The role of the message sender.
     * Can be "system", "user", or "assistant".
     */
    private String role;
    
    /**
     * The content of the message.
     */
    private String content;
    
    /**
     * Creates a new user message with the given content.
     * 
     * @param content The message content
     * @return A new Message instance with role "user"
     */
    public static Message userMessage(String content) {
        return Message.builder()
                .role("user")
                .content(content)
                .build();
    }
    
    /**
     * Creates a new system message with the given content.
     * 
     * @param content The message content
     * @return A new Message instance with role "system"
     */
    public static Message systemMessage(String content) {
        return Message.builder()
                .role("system")
                .content(content)
                .build();
    }
}