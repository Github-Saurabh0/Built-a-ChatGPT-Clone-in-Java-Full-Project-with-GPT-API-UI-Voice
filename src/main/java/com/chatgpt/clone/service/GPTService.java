package com.chatgpt.clone.service;

import com.chatgpt.clone.config.OpenAIConfig;
import com.chatgpt.clone.exception.OpenAIException;
import com.chatgpt.clone.model.ChatCompletionRequest;
import com.chatgpt.clone.model.ChatCompletionResponse;
import com.chatgpt.clone.model.Message;
import com.chatgpt.clone.util.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Service class for interacting with the OpenAI GPT API.
 * Handles sending requests and processing responses.
 */
public class GPTService {
    private static final Logger logger = new Logger(GPTService.class);
    
    private final OpenAIConfig config;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    
    /**
     * Default model to use for chat completions.
     */
    private static final String DEFAULT_MODEL = "gpt-3.5-turbo";
    
    /**
     * Creates a new GPTService with the provided configuration.
     * 
     * @param config The OpenAI API configuration
     */
    public GPTService(OpenAIConfig config) {
        this.config = config;
        this.objectMapper = new ObjectMapper();
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        
        logger.info("GPTService initialized with API URL: " + config.getApiUrl());
    }
    
    /**
     * Creates a new GPTService with a default configuration.
     */
    public GPTService() {
        this(new OpenAIConfig());
    }
    
    /**
     * Sends a question to the GPT model and returns the response.
     * 
     * @param question The user's question
     * @return The model's response as a String
     * @throws OpenAIException If an error occurs during the API call
     */
    public String askQuestion(String question) {
        logger.debug("Asking question using default model: " + DEFAULT_MODEL);
        return askQuestion(question, DEFAULT_MODEL);
    }
    
    /**
     * Sends a question to the specified GPT model and returns the response.
     * 
     * @param question The user's question
     * @param model The model to use (e.g., "gpt-3.5-turbo", "gpt-4")
     * @return The model's response as a String
     * @throws OpenAIException If an error occurs during the API call
     */
    public String askQuestion(String question, String model) {
        logger.debug("Asking question using model: " + model);
        List<Message> messages = new ArrayList<>();
        messages.add(Message.userMessage(question));
        
        return sendChatCompletionRequest(messages, model);
    }
    
    /**
     * Sends a conversation to the GPT model and returns the response.
     * 
     * @param messages The list of messages in the conversation
     * @return The model's response as a String
     * @throws OpenAIException If an error occurs during the API call
     */
    public String sendConversation(List<Message> messages) {
        logger.debug("Sending conversation with " + messages.size() + " messages using default model: " + DEFAULT_MODEL);
        return sendChatCompletionRequest(messages, DEFAULT_MODEL);
    }
    
    /**
     * Sends a conversation to the specified GPT model and returns the response.
     * 
     * @param messages The list of messages in the conversation
     * @param model The model to use (e.g., "gpt-3.5-turbo", "gpt-4")
     * @return The model's response as a String
     * @throws OpenAIException If an error occurs during the API call
     */
    public String sendConversation(List<Message> messages, String model) {
        logger.debug("Sending conversation with " + messages.size() + " messages using model: " + model);
        return sendChatCompletionRequest(messages, model);
    }
    
    /**
     * Sends a chat completion request to the OpenAI API.
     * 
     * @param messages The list of messages to send
     * @param model The model to use
     * @return The model's response as a String
     * @throws OpenAIException If an error occurs during the API call
     */
    private String sendChatCompletionRequest(List<Message> messages, String model) {
        // Check if we're using a placeholder API key
        if (config.getApiKey().equals("sk-your-api-key-here")) {
            logger.info("Using placeholder API key - returning mock response");
            return generateMockResponse(messages);
        }
        
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(model)
                .messages(messages)
                .build();
        
        try {
            String requestJson = objectMapper.writeValueAsString(request);
            RequestBody body = RequestBody.create(requestJson, JSON);
            
            logger.debug("Sending request to OpenAI API: " + config.getApiUrl());
            
            Request httpRequest = new Request.Builder()
                    .url(config.getApiUrl())
                    .addHeader("Authorization", "Bearer " + config.getApiKey())
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();
            
            try (Response response = client.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No response body";
                    int statusCode = response.code();
                    String errorType = "unknown";
                    String errorMessage = errorBody;
                    
                    logger.error("OpenAI API error response: " + statusCode + " - " + errorBody);
                    
                    // Try to parse error details from JSON response
                    try {
                        JsonNode errorJson = objectMapper.readTree(errorBody);
                        if (errorJson.has("error")) {
                            JsonNode error = errorJson.get("error");
                            if (error.has("type")) {
                                errorType = error.get("type").asText();
                            }
                            if (error.has("message")) {
                                errorMessage = error.get("message").asText();
                            }
                        }
                    } catch (Exception e) {
                        // If we can't parse the error JSON, just use the raw error body
                        logger.debug("Could not parse error JSON: " + e.getMessage());
                    }
                    
                    throw new OpenAIException(statusCode, errorType, errorMessage);
                }
                
                if (response.body() == null) {
                    throw new OpenAIException("Response body is null");
                }
                
                String responseBody = response.body().string();
                logger.debug("Received response from OpenAI API");
                
                ChatCompletionResponse completionResponse = objectMapper.readValue(responseBody, ChatCompletionResponse.class);
                
                String content = completionResponse.getFirstChoiceContent();
                if (content == null) {
                    logger.error("No content in OpenAI API response");
                    throw new OpenAIException("No content in response");
                }
                
                logger.debug("Successfully processed OpenAI API response");
                return content;
            }
        } catch (IOException e) {
            logger.error("Error communicating with OpenAI API", e);
            throw new OpenAIException("Error communicating with OpenAI API", e);
        }
    }
    
    /**
     * Generates a mock response for testing when using a placeholder API key.
     * 
     * @param messages The list of messages in the conversation
     * @return A mock response string
     */
    private String generateMockResponse(List<Message> messages) {
        // Get the last user message
        String lastUserMessage = "";
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message message = messages.get(i);
            if ("user".equals(message.getRole())) {
                lastUserMessage = message.getContent();
                break;
            }
        }
        
        // Generate a simple response based on the user's message
        if (lastUserMessage.toLowerCase().contains("hello") || 
            lastUserMessage.toLowerCase().contains("hi")) {
            return "Hello! I'm a mock AI assistant. Since you're using a placeholder API key, I'm providing simulated responses for testing purposes.";
        } else if (lastUserMessage.toLowerCase().contains("how are you")) {
            return "I'm just a simulated response for testing purposes, but thanks for asking!";
        } else if (lastUserMessage.toLowerCase().contains("weather")) {
            return "I can't check the actual weather since this is a simulated response. In a real implementation, I would connect to the OpenAI API to provide accurate information.";
        } else if (lastUserMessage.toLowerCase().contains("name")) {
            return "I'm a simulated ChatGPT response for testing purposes. In a real implementation, I would be powered by OpenAI's GPT models.";
        } else if (lastUserMessage.toLowerCase().contains("??")) {
            return "I notice you've used multiple question marks. This is a simulated response since you're using a placeholder API key. For real responses, please configure a valid OpenAI API key.";
        } else if (lastUserMessage.endsWith("?")) {
            return "That's an interesting question! This is a simulated response for testing purposes. With a valid API key, you would receive an actual response from OpenAI's GPT models.";
        } else {
            return "This is a simulated response since you're using a placeholder API key. For real AI-powered responses, please configure a valid OpenAI API key in your config.properties file or as an environment variable.";
        }
    }
}