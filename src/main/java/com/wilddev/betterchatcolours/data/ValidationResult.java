package com.wilddev.betterchatcolours.data;

public class ValidationResult {
    private final boolean valid;
    private final String message;
    
    public ValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public String getMessage() {
        return message;
    }
    
    @Override
    public String toString() {
        return "ValidationResult{valid=" + valid + ", message='" + message + "'}";
    }
}
