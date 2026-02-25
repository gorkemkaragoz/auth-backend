package com.gorkem.auth_service.security;

import com.gorkem.auth_service.entities.Role;
import com.gorkem.auth_service.entities.User;
import com.gorkem.auth_service.repos.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.ROLE_MEMBER);
    }

    @Test
    @DisplayName("Var olan email ile UserDetails basariyla donmeli")
    void shouldLoadUserByUsernameSuccessfully() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@example.com");

        // Then
        assertThat(userDetails.getUsername()).isEqualTo("test@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_MEMBER");
    }

    @Test
    @DisplayName("Admin rolu ile UserDetails dogru authority icermeli")
    void shouldLoadAdminUserWithCorrectAuthority() {
        // Given
        user.setRole(Role.ROLE_ADMIN);
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("admin@example.com");

        // Then
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Olmayan email ile UsernameNotFoundException firlatmali")
    void shouldThrowUsernameNotFoundExceptionWhenEmailNotFound() {
        // Given
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("notfound@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}