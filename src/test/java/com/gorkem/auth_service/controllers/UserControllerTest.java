package com.gorkem.auth_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gorkem.auth_service.dto.UserResponse;
import com.gorkem.auth_service.dto.UserSaveRequest;
import com.gorkem.auth_service.dto.UserUpdateRequest;
import com.gorkem.auth_service.entities.Role;
import com.gorkem.auth_service.config.SecurityConfig;
import com.gorkem.auth_service.security.CustomUserDetailsService;
import com.gorkem.auth_service.security.JwtService;
import com.gorkem.auth_service.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userResponse = new UserResponse(1L, "John", "Doe", "john@example.com", Role.ROLE_MEMBER);
    }

    // --- GET /users/{userId} ---

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /users/{id} - admin ile basarili istek 200 donmeli")
    void shouldGetOneUserSuccessfully() throws Exception {
        // Given
        when(userService.getOneUser(1L)).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.role").value("ROLE_MEMBER"));
    }

    @Test
    @DisplayName("GET /users/{id} - token olmadan 401/403 donmeli")
    void shouldReturn403WhenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/users/1"))
                .andExpect(status().is4xxClientError());
    }

    // --- GET /users ---

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /users - admin ile tum kullanicilar listelenmeli")
    void shouldGetAllUsersSuccessfully() throws Exception {
        // Given
        UserResponse user2 = new UserResponse(2L, "Jane", "Doe", "jane@example.com", Role.ROLE_MEMBER);
        when(userService.getAllUsers()).thenReturn(List.of(userResponse, user2));

        // When & Then
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].email").value("john@example.com"))
                .andExpect(jsonPath("$[1].email").value("jane@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /users - hic kullanici yoksa bos liste donmeli")
    void shouldReturnEmptyListWhenNoUsers() throws Exception {
        // Given
        when(userService.getAllUsers()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // --- POST /users ---

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /users - admin ile kullanici olusturulmali")
    void shouldCreateUserSuccessfully() throws Exception {
        // Given
        UserSaveRequest request = new UserSaveRequest("John", "Doe", "john@example.com", "password123");
        when(userService.createUser(any(UserSaveRequest.class))).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    // --- PUT /users/{userId} ---

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /users/{id} - admin ile kullanici guncellenmeli")
    void shouldUpdateUserSuccessfully() throws Exception {
        // Given
        UserUpdateRequest request = new UserUpdateRequest("UpdatedName", "UpdatedSurname", "updated@example.com");
        UserResponse updatedResponse = new UserResponse(1L, "UpdatedName", "UpdatedSurname", "updated@example.com", Role.ROLE_MEMBER);
        when(userService.updateUser(eq(1L), any(UserUpdateRequest.class))).thenReturn(updatedResponse);

        // When & Then
        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("UpdatedName"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    @DisplayName("PUT /users/{id} - ROLE_MEMBER ile istek 403 donmeli")
    void shouldReturn403WhenMemberTriesToUpdate() throws Exception {
        // Given
        UserUpdateRequest request = new UserUpdateRequest("Name", "Surname", "email@example.com");

        // When & Then
        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // --- DELETE /users/{userId} ---

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /users/{id} - admin ile kullanici silinmeli")
    void shouldDeleteUserSuccessfully() throws Exception {
        // Given
        doNothing().when(userService).deleteUser(1L);

        // When & Then
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    @DisplayName("DELETE /users/{id} - ROLE_MEMBER ile istek 403 donmeli")
    void shouldReturn403WhenMemberTriesToDelete() throws Exception {
        // When & Then
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isForbidden());

        verify(userService, never()).deleteUser(any());
    }
}