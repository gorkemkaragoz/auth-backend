package com.gorkem.auth_service.services;

import com.gorkem.auth_service.dto.*;
import com.gorkem.auth_service.entities.Role;
import com.gorkem.auth_service.entities.User;
import com.gorkem.auth_service.repos.UserRepository;
import com.gorkem.auth_service.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

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
            throw new RuntimeException("Bu email zaten kayıtlı!");
        }

        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.ROLE_MEMBER); //

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
        // Spring Security ile kimlik doğrulama yapıyoruz
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));

        // Her şey doğruysa token üret
        String jwtToken = jwtService.generateToken(user.getEmail());

        return new AuthResponse(
                jwtToken,
                user.getId(),
                user.getEmail(),
                user.getRole()
        );

    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        // 1. Kullanıcı var mı kontrol et
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Bu email ile kayıtlı kullanıcı bulunamadı!"));

        // 2. 6 haneli kod üret ve süre ata
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        user.setOtpCode(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5)); //

        userRepository.save(user);

        // 3. Maili gönder
        try {
            mailService.sendOtpMail(user.getEmail(), otp);
        } catch (Exception e) {
            System.out.println("Mail gönderilemedi ama OTP kaydedildi: " + e.getMessage());
        }
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));

        if (user.getOtpCode() == null || !user.getOtpCode().equals(request.otpCode())) {
            throw new RuntimeException("Geçersiz doğrulama kodu!");
        }

        if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Kodun süresi dolmuş, Patron!");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword())); //
        user.setOtpCode(null);
        user.setOtpExpiry(null);

        userRepository.save(user);
    }
}