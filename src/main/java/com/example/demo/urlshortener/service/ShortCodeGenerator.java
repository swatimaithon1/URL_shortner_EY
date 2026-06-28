package com.example.demo.urlshortener.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class ShortCodeGenerator {

    // URL-safe alpha-numeric character set used for generated codes.
    private static final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    private final SecureRandom random = new SecureRandom();

    public String generate(int length) {
        // Guards minimum entropy and aligns with routing regex constraints.
        if (length < 4) {
            throw new IllegalArgumentException("length must be >= 4");
        }
        StringBuilder code = new StringBuilder(length);
        // Picks each character independently for uniform distribution.
        for (int i = 0; i < length; i++) {
            code.append(ALPHABET[random.nextInt(ALPHABET.length)]);
        }
        return code.toString();
    }
}

