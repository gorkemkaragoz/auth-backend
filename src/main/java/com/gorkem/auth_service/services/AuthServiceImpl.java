package com.gorkem.auth_service.services;

import com.gorkem.auth_service.dto.*;
import com.gorkem.auth_service.entities.Role;
import com.gorkem.auth_service.entities.User;
import com.gorkem.auth_service.exception.EmailAlreadyExistsException;
import com.gorkem.auth_service.exception.InvalidOtpException;
import com.gorkem.auth_service.exception.OtpExpiredException;
import com.gorkem.auth_service.exception.UserNotFoundException;
import com.gorkem.auth_service.repos.UserRepository;
import com.gorkem.auth_service.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final MailService mailService;

    @Override
    public UserResponse register(AuthRegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("This email address is already registered.");
        }

        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.ROLE_MEMBER);

        User savedUser = userRepository.save(user);
        return new UserResponse(
                savedUser.getId(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getEmail(),
                savedUser.getRole()
        );
    }

    @Override
    public AuthResponse login(AuthLoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        String jwtToken = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(
                jwtToken,
                user.getId(),
                user.getEmail(),
                user.getRole()
        );
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException("No account found with this email address."));

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        user.setOtpCode(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));

        userRepository.save(user);

        try {
            mailService.sendOtpMail(user.getEmail(), otp);
        } catch (Exception e) {
            log.warn("Mail gönderilemedi ama OTP kaydedildi: {}", e.getMessage());
        }
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        if (user.getOtpCode() == null || !user.getOtpCode().equals(request.otpCode())) {
            throw new InvalidOtpException("Invalid verification code.");
        }

        if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new OtpExpiredException("Verification code has expired. Please request a new one.");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setOtpCode(null);
        user.setOtpExpiry(null);

        userRepository.save(user);
    }
}
