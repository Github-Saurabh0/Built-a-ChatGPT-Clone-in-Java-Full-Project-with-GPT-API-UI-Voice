package com.chatgpt.clone.config;

import lombok.Getter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Configuration class for OpenAI API settings.
 * Provides methods to load API key from properties file or environment variables.
 */
@Getter
public class OpenAIConfig {
    private static final String CONFIG_FILE = "config.properties";
    private static final String ENV_API_KEY = "OPENAI_API_KEY";
    private static final String PROP_API_KEY = "openai.api.key";
    
    private final String apiKey;
    private final String apiUrl;
    
    /**
     * Creates a new OpenAIConfig instance with default API URL.
     */
    public OpenAIConfig() {
        this.apiKey = loadApiKey();
        this.apiUrl = "https://api.openai.com/v1/chat/completions";
    }
    
    /**
     * Creates a new OpenAIConfig instance with custom API URL.
     * 
     * @param apiUrl The custom API URL to use
     */
    public OpenAIConfig(String apiUrl) {
        this.apiKey = loadApiKey();
        this.apiUrl = apiUrl;
    }
    
    /**
     * Loads the API key from environment variables or properties file.
     * Environment variables take precedence over the properties file.
     * 
     * @return The API key as a String
     * @throws RuntimeException if the API key cannot be found
     */
    private String loadApiKey() {
        // First try to load from environment variable
        String apiKey = System.getenv(ENV_API_KEY);
        if (apiKey != null && !apiKey.isEmpty()) {
            return apiKey;
        }
        
        // Then try to load from properties file
        try {
            Path configPath = Paths.get(CONFIG_FILE);
            if (Files.exists(configPath)) {
                Properties props = new Properties();
                props.load(Files.newBufferedReader(configPath));
                apiKey = props.getProperty(PROP_API_KEY);
                if (apiKey != null && !apiKey.isEmpty()) {
                    return apiKey;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load API key from properties file", e);
        }
        
        throw new RuntimeException("API key not found. Please set it in environment variable " + 
                ENV_API_KEY + " or in properties file " + CONFIG_FILE);
    }
}