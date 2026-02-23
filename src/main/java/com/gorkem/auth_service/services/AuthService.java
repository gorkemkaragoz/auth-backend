package com.gorkem.auth_service.services;

import com.gorkem.auth_service.dto.*;

public interface AuthService {
    UserResponse register(AuthRegisterRequest request);
    AuthResponse login(AuthLoginRequest request);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}