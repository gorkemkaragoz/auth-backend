package com.gorkem.auth_service.dto;

public record UserSaveRequest(
        String firstName,
        String lastName,
        String email,
        String password
) {}
