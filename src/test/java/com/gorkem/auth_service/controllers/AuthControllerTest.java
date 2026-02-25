package com.gorkem.auth_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gorkem.auth_service.dto.*;
import com.gorkem.auth_service.entities.Role;
import com.gorkem.auth_service.security.CustomUserDetailsService;
import com.gorkem.auth_service.security.JwtAuthFilter;
import com.gorkem.auth_service.security.JwtService;
import com.gorkem.auth_service.services.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    // Security icin gerekli mock'lar
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    // --- POST /auth/register ---

    @Test
    @DisplayName("POST /auth/register - basarili kayit 200 donmeli")
    void shouldRegisterSuccessfully() throws Exception {
        // Given
        AuthRegisterRequest request = new AuthRegisterRequest("John", "Doe", "john@example.com", "password123");
        UserResponse response = new UserResponse(1L, "John", "Doe", "john@example.com", Role.ROLE_MEMBER);

        when(authService.register(any(AuthRegisterRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.role").value("ROLE_MEMBER"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("POST /auth/register - servis exception firlatirsa 500 donmeli")
    void shouldReturn500WhenRegisterFails() throws Exception {
        // Given
        AuthRegisterRequest request = new AuthRegisterRequest("John", "Doe", "john@example.com", "password123");
        when(authService.register(any())).thenThrow(new RuntimeException("Email already exists"));

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    // --- POST /auth/login ---

    @Test
    @DisplayName("POST /auth/login - basarili giris token donmeli")
    void shouldLoginSuccessfully() throws Exception {
        // Given
        AuthLoginRequest request = new AuthLoginRequest("john@example.com", "password123");
        AuthResponse response = new AuthResponse("mocked.jwt.token", 1L, "john@example.com", Role.ROLE_MEMBER);

        when(authService.login(any(AuthLoginRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked.jwt.token"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.role").value("ROLE_MEMBER"));
    }

    @Test
    @DisplayName("POST /auth/login - yanlis kimlik bilgileri 500 donmeli")
    void shouldReturn500WhenLoginFails() throws Exception {
        // Given
        AuthLoginRequest request = new AuthLoginRequest("john@example.com", "wrongpassword");
        when(authService.login(any())).thenThrow(new RuntimeException("Bad credentials"));

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    // --- POST /auth/forgot-password ---

    @Test
    @DisplayName("POST /auth/forgot-password - basarili istek 200 donmeli")
    void shouldHandleForgotPasswordSuccessfully() throws Exception {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest("john@example.com");
        doNothing().when(authService).forgotPassword(any(ForgotPasswordRequest.class));

        // When & Then
        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authService, times(1)).forgotPassword(any(ForgotPasswordRequest.class));
    }

    // --- POST /auth/reset-password ---

    @Test
    @DisplayName("POST /auth/reset-password - basarili istek 200 donmeli")
    void shouldHandleResetPasswordSuccessfully() throws Exception {
        // Given
        ResetPasswordRequest request = new ResetPasswordRequest("john@example.com", "123456", "newPassword123");
        doNothing().when(authService).resetPassword(any(ResetPasswordRequest.class));

        // When & Then
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authService, times(1)).resetPassword(any(ResetPasswordRequest.class));
    }
}