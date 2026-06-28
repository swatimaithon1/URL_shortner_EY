package com.example.demo.urlshortener.exception;

// Thrown when a requested shortcode does not exist in storage.
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}

