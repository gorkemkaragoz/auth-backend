package com.gorkem.auth_service.dto;

public record AuthLoginRequest(
        String email,
        String password
) {}