package com.example.demo.urlshortener.validation;

import com.example.demo.urlshortener.exception.InvalidUrlException;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

@Component
public class UrlSafetyValidator {

    public URI validateAndParse(String rawUrl) {
        URI uri;
        try {
            // Parse once and reuse the normalized URI object for all checks.
            uri = new URI(rawUrl);
        } catch (URISyntaxException exception) {
            throw new InvalidUrlException("url is not a valid URI");
        }

        // Absolute URLs must include both scheme and host.
        if (uri.getScheme() == null || uri.getHost() == null) {
            throw new InvalidUrlException("url must include scheme and host");
        }

        String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
        // Restrict protocols to reduce SSRF and unexpected handler behavior.
        if (!scheme.equals("http") && !scheme.equals("https")) {
            throw new InvalidUrlException("only http and https URLs are allowed");
        }

        String host = uri.getHost().toLowerCase(Locale.ROOT);
        // Blocks obvious local-network targets by hostname.
        if (host.equals("localhost") || host.endsWith(".local")) {
            throw new InvalidUrlException("local network targets are not allowed");
        }

        // Blocks private, loopback, and link-local IP ranges.
        if (isPrivateIpv4(host) || isPrivateIpv6(host)) {
            throw new InvalidUrlException("private IP targets are not allowed");
        }

        return uri;
    }

    private boolean isPrivateIpv4(String host) {
        // Non-IPv4 hostnames bypass this branch and are evaluated elsewhere.
        String[] tokens = host.split("\\.");
        if (tokens.length != 4) {
            return false;
        }

        int[] parts = new int[4];
        for (int i = 0; i < tokens.length; i++) {
            try {
                parts[i] = Integer.parseInt(tokens[i]);
            } catch (NumberFormatException exception) {
                return false;
            }
            if (parts[i] < 0 || parts[i] > 255) {
                return false;
            }
        }

        return parts[0] == 10
                || (parts[0] == 172 && parts[1] >= 16 && parts[1] <= 31)
                || (parts[0] == 192 && parts[1] == 168)
                || (parts[0] == 127)
                || (parts[0] == 169 && parts[1] == 254);
    }

    private boolean isPrivateIpv6(String host) {
        // URI parser returns IPv6 hosts in bracket notation.
        if (!host.startsWith("[") || !host.endsWith("]")) {
            return false;
        }

        String normalized = host.substring(1, host.length() - 1).toLowerCase(Locale.ROOT);
        return normalized.equals("::1") || normalized.startsWith("fc") || normalized.startsWith("fd") || normalized.startsWith("fe80");
    }
}

