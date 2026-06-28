package com.example.demo.urlshortener.exception;

// Thrown when a new shortcode collides with an existing one.
public class ResourceConflictException extends RuntimeException {

    public ResourceConflictException(String message) {
        super(message);
    }
}

