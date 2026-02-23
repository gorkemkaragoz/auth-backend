package com.gorkem.auth_service.dto;

public record UserUpdateRequest(
        String firstName,
        String lastName,
        String email
) {}