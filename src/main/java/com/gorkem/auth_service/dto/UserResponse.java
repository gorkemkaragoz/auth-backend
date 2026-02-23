package com.gorkem.auth_service.dto;

import com.gorkem.auth_service.entities.Role;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        Role role
) {}
