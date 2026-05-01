package com.pghpizza.api.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String frontendBaseUrl,
        Jwt jwt,
        Admin admin,
        Mail mail,
        Cors cors
) {
    public record Jwt(String issuer, String secret, long expiresMinutes) {
    }

    public record Admin(String email, String password, String displayName) {
    }

    public record Mail(String from, boolean smtpEnabled) {
    }

    public record Cors(List<String> allowedOrigins) {
    }
}
