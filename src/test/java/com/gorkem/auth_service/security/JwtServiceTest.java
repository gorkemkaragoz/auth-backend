package com.gorkem.auth_service.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET = "mysecretkeymysecretkeymysecretkeymysecretkey"; // min 32 char
    private static final long EXPIRATION_MS = 1000 * 60 * 60; // 1 saat

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(SECRET, EXPIRATION_MS);
        jwtService = new JwtService(properties);
    }

    @Test
    @DisplayName("Token uretildiginde email claim icinde olmali")
    void shouldGenerateTokenWithEmail() {
        // When
        String token = jwtService.generateToken("test@example.com", "ROLE_MEMBER");

        // Then
        assertThat(token).isNotNull();
        assertThat(jwtService.extractEmail(token)).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Token uretildiginde rol claim icinde olmali")
    void shouldGenerateTokenWithRole() {
        // When
        String token = jwtService.generateToken("test@example.com", "ROLE_ADMIN");

        // Then
        assertThat(jwtService.extractRole(token)).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Gecerli token ve dogru email ile isTokenValid true donmeli")
    void shouldReturnTrueWhenTokenIsValid() {
        // Given
        String token = jwtService.generateToken("test@example.com", "ROLE_MEMBER");

        // When & Then
        assertThat(jwtService.isTokenValid(token, "test@example.com")).isTrue();
    }

    @Test
    @DisplayName("Farkli email ile isTokenValid false donmeli")
    void shouldReturnFalseWhenEmailDoesNotMatch() {
        // Given
        String token = jwtService.generateToken("test@example.com", "ROLE_MEMBER");

        // When & Then
        assertThat(jwtService.isTokenValid(token, "other@example.com")).isFalse();
    }

    @Test
    @DisplayName("Suresi dolmus token gecersiz olmali")
    void shouldReturnFalseWhenTokenIsExpired() throws InterruptedException {
        // Given
        JwtProperties shortExpiry = new JwtProperties(SECRET, 1L); // 1ms
        JwtService shortJwtService = new JwtService(shortExpiry);
        String token = shortJwtService.generateToken("test@example.com", "ROLE_MEMBER");

        // When
        Thread.sleep(10);

        // Then
        assertThat(shortJwtService.isTokenValid(token, "test@example.com")).isFalse();
    }

    @Test
    @DisplayName("Admin rolu ile token uretilip rol dogru cekilmeli")
    void shouldExtractAdminRole() {
        // When
        String token = jwtService.generateToken("admin@example.com", "ROLE_ADMIN");

        // Then
        assertThat(jwtService.extractRole(token)).isEqualTo("ROLE_ADMIN");
    }
}