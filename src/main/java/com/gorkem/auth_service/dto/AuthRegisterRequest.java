package com.gorkem.auth_service.dto;

public record AuthRegisterRequest(
        String firstName,
        String lastName,
        String email,
        String password
) {}