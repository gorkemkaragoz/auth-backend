package com.gorkem.auth_service.dto;

public record ResetPasswordRequest(
        String email,
        String otpCode,
        String newPassword
) {}
