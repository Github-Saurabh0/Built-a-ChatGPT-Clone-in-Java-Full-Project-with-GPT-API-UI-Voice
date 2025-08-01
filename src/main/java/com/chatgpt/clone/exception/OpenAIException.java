package com.chatgpt.clone.exception;

/**
 * Exception class for OpenAI API related errors.
 * Provides more specific error handling for OpenAI API calls.
 */
public class OpenAIException extends RuntimeException {

    private final int statusCode;
    private final String errorType;

    /**
     * Creates a new OpenAIException with the specified status code, error type, and message.
     * 
     * @param statusCode The HTTP status code from the API response
     * @param errorType The error type from the API response
     * @param message The error message
     */
    public OpenAIException(int statusCode, String errorType, String message) {
        super(message);
        this.statusCode = statusCode;
        this.errorType = errorType;
    }

    /**
     * Creates a new OpenAIException with the specified message and cause.
     * 
     * @param message The error message
     * @param cause The cause of the exception
     */
    public OpenAIException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
        this.errorType = "unknown";
    }

    /**
     * Creates a new OpenAIException with the specified message.
     * 
     * @param message The error message
     */
    public OpenAIException(String message) {
        super(message);
        this.statusCode = 0;
        this.errorType = "unknown";
    }

    /**
     * Gets the HTTP status code from the API response.
     * 
     * @return The status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Gets the error type from the API response.
     * 
     * @return The error type
     */
    public String getErrorType() {
        return errorType;
    }

    @Override
    public String toString() {
        if (statusCode > 0) {
            return String.format("OpenAIException[status=%d, error=%s]: %s", 
                    statusCode, errorType, getMessage());
        } else {
            return super.toString();
        }
    }
}