package com.example.demo.urlshortener.exception;

// Thrown when a shortcode exists but its expiration time has passed.
public class ResourceExpiredException extends RuntimeException {

    public ResourceExpiredException(String message) {
        super(message);
    }
}

