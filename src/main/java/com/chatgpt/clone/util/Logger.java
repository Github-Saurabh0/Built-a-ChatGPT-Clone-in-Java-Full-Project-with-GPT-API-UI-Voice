package com.chatgpt.clone.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

/**
 * Logger utility class for the application.
 * Provides a simple wrapper around Java's built-in logging.
 */
public class Logger {
    private final java.util.logging.Logger logger;
    
    static {
        try (InputStream is = Logger.class.getClassLoader().getResourceAsStream("logging.properties")) {
            if (is != null) {
                LogManager.getLogManager().readConfiguration(is);
            }
        } catch (IOException e) {
            System.err.println("Could not load logging.properties file");
            e.printStackTrace();
        }
    }
    
    /**
     * Creates a new Logger for the specified class.
     * 
     * @param clazz The class to create the logger for
     */
    public Logger(Class<?> clazz) {
        this.logger = java.util.logging.Logger.getLogger(clazz.getName());
    }
    
    /**
     * Logs an informational message.
     * 
     * @param message The message to log
     */
    public void info(String message) {
        logger.info(message);
    }
    
    /**
     * Logs a warning message.
     * 
     * @param message The message to log
     */
    public void warning(String message) {
        logger.warning(message);
    }
    
    /**
     * Logs an error message.
     * 
     * @param message The message to log
     */
    public void error(String message) {
        logger.severe(message);
    }
    
    /**
     * Logs an error message with an exception.
     * 
     * @param message The message to log
     * @param throwable The exception to log
     */
    public void error(String message, Throwable throwable) {
        logger.severe(message);
        logger.severe(throwable.getMessage());
        for (StackTraceElement element : throwable.getStackTrace()) {
            logger.severe("\tat " + element);
        }
    }
    
    /**
     * Logs a debug message.
     * 
     * @param message The message to log
     */
    public void debug(String message) {
        logger.fine(message);
    }
}