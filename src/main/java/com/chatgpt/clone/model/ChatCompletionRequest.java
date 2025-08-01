package com.chatgpt.clone.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a request to the OpenAI chat completion API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatCompletionRequest {
    /**
     * ID of the model to use.
     * Example: "gpt-3.5-turbo" or "gpt-4"
     */
    private String model;
    
    /**
     * The messages to generate chat completions for.
     */
    private List<Message> messages;
    
    /**
     * What sampling temperature to use, between 0 and 2.
     * Higher values like 0.8 will make the output more random,
     * while lower values like 0.2 will make it more focused and deterministic.
     */
    @Builder.Default
    private double temperature = 0.7;
    
    /**
     * The maximum number of tokens to generate in the chat completion.
     */
    @Builder.Default
    private int max_tokens = 1000;
    
    /**
     * Whether to stream back partial progress.
     */
    @Builder.Default
    private boolean stream = false;
}