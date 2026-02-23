package com.gorkem.auth_service.dto;

import com.gorkem.auth_service.entities.Role;

public record AuthResponse(
        String token,
        Long id,
        String email,
        Role role
) {}