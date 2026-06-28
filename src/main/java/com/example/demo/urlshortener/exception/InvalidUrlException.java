package com.example.demo.urlshortener.exception;

// Thrown when the submitted URL fails syntax or safety checks.
public class InvalidUrlException extends RuntimeException {

    public InvalidUrlException(String message) {
        super(message);
    }
}

