package com.gorkem.auth_service.services;

import com.gorkem.auth_service.dto.*;
import com.gorkem.auth_service.entities.Role;
import com.gorkem.auth_service.entities.User;
import com.gorkem.auth_service.repos.UserRepository;
import com.gorkem.auth_service.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private MailService mailService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@example.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.ROLE_MEMBER);
    }

    // --- register ---

    @Test
    @DisplayName("Yeni email ile kayit basariyla tamamlanmali")
    void shouldRegisterSuccessfully() {
        // Given
        AuthRegisterRequest request = new AuthRegisterRequest("John", "Doe", "john@example.com", "password123");
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        UserResponse response = authService.register(request);

        // Then
        assertThat(response.email()).isEqualTo("john@example.com");
        assertThat(response.role()).isEqualTo(Role.ROLE_MEMBER);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Zaten kayitli email ile kayit RuntimeException firlatmali")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        AuthRegisterRequest request = new AuthRegisterRequest("John", "Doe", "john@example.com", "password123");
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class);
        verify(userRepository, never()).save(any(User.class));
    }

    // --- login ---

    @Test
    @DisplayName("Dogru kimlik bilgileri ile login basarili olmali ve token donmeli")
    void shouldLoginSuccessfullyAndReturnToken() {
        // Given
        AuthLoginRequest request = new AuthLoginRequest("john@example.com", "password123");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken("john@example.com", "ROLE_MEMBER")).thenReturn("mocked.jwt.token");

        // When
        AuthResponse response = authService.login(request);

        // Then
        assertThat(response.token()).isEqualTo("mocked.jwt.token");
        assertThat(response.email()).isEqualTo("john@example.com");
        assertThat(response.role()).isEqualTo(Role.ROLE_MEMBER);
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Yanlis kimlik bilgileri ile login BadCredentialsException firlatmali")
    void shouldThrowExceptionWhenCredentialsAreInvalid() {
        // Given
        AuthLoginRequest request = new AuthLoginRequest("john@example.com", "wrongpassword");
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
        verify(jwtService, never()).generateToken(anyString(), anyString());
    }

    // --- forgotPassword ---

    @Test
    @DisplayName("Var olan email ile OTP olusturulup kaydedilmeli ve mail gonderilmeli")
    void shouldSendOtpWhenEmailExists() throws Exception {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest("john@example.com");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        authService.forgotPassword(request);

        // Then
        verify(userRepository, times(1)).save(any(User.class));
        verify(mailService, times(1)).sendOtpMail(eq("john@example.com"), anyString());
    }

    @Test
    @DisplayName("Olmayan email ile forgotPassword RuntimeException firlatmali")
    void shouldThrowExceptionWhenEmailNotFoundInForgotPassword() {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest("notfound@example.com");
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.forgotPassword(request))
                .isInstanceOf(RuntimeException.class);
        verify(mailService, never()).sendOtpMail(anyString(), anyString());
    }

    @Test
    @DisplayName("Mail gonderilemese bile OTP kaydedilmeli")
    void shouldSaveOtpEvenWhenMailFails() throws Exception {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest("john@example.com");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        doThrow(new RuntimeException("SMTP error")).when(mailService).sendOtpMail(anyString(), anyString());

        // When (exception firlatmamali)
        authService.forgotPassword(request);

        // Then
        verify(userRepository, times(1)).save(any(User.class));
    }

    // --- resetPassword ---

    @Test
    @DisplayName("Gecerli OTP ile sifre basariyla sifirlannmali")
    void shouldResetPasswordSuccessfully() {
        // Given
        user.setOtpCode("123456");
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        ResetPasswordRequest request = new ResetPasswordRequest("john@example.com", "123456", "newPassword123");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        authService.resetPassword(request);

        // Then
        verify(passwordEncoder, times(1)).encode("newPassword123");
        verify(userRepository, times(1)).save(any(User.class));
        assertThat(user.getOtpCode()).isNull();
        assertThat(user.getOtpExpiry()).isNull();
    }

    @Test
    @DisplayName("Gecersiz OTP kodu ile resetPassword RuntimeException firlatmali")
    void shouldThrowExceptionWhenOtpIsInvalid() {
        // Given
        user.setOtpCode("123456");
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        ResetPasswordRequest request = new ResetPasswordRequest("john@example.com", "999999", "newPassword123");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        // When & Then
        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(RuntimeException.class);
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Suresi dolmus OTP ile resetPassword RuntimeException firlatmali")
    void shouldThrowExceptionWhenOtpIsExpired() {
        // Given
        user.setOtpCode("123456");
        user.setOtpExpiry(LocalDateTime.now().minusMinutes(1)); // gecmis
        ResetPasswordRequest request = new ResetPasswordRequest("john@example.com", "123456", "newPassword123");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        // When & Then
        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(RuntimeException.class);
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Null OTP kodu ile resetPassword RuntimeException firlatmali")
    void shouldThrowExceptionWhenOtpIsNull() {
        // Given
        user.setOtpCode(null);
        ResetPasswordRequest request = new ResetPasswordRequest("john@example.com", "123456", "newPassword123");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        // When & Then
        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(RuntimeException.class);
    }
}