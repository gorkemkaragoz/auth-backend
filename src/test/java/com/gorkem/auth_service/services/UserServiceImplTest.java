package com.gorkem.auth_service.services;

import com.gorkem.auth_service.dto.UserResponse;
import com.gorkem.auth_service.dto.UserSaveRequest;
import com.gorkem.auth_service.dto.UserUpdateRequest;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

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

    // --- getOneUser ---

    @Test
    @DisplayName("Var olan userId ile kullanici basariyla donmeli")
    void shouldGetOneUserSuccessfully() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        UserResponse response = userService.getOneUser(1L);

        // Then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.firstName()).isEqualTo("John");
        assertThat(response.email()).isEqualTo("john@example.com");
        assertThat(response.role()).isEqualTo(Role.ROLE_MEMBER);
    }

    @Test
    @DisplayName("Olmayan userId ile RuntimeException firlatmali")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getOneUser(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Kullanıcı bulunamadı!");
    }

    // --- getAllUsers ---

    @Test
    @DisplayName("Tum kullanicilar listesi basariyla donmeli")
    void shouldGetAllUsersSuccessfully() {
        // Given
        User user2 = new User();
        user2.setId(2L);
        user2.setFirstName("Jane");
        user2.setLastName("Doe");
        user2.setEmail("jane@example.com");
        user2.setRole(Role.ROLE_MEMBER);

        when(userRepository.findAll()).thenReturn(List.of(user, user2));

        // When
        List<UserResponse> responses = userService.getAllUsers();

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).email()).isEqualTo("john@example.com");
        assertThat(responses.get(1).email()).isEqualTo("jane@example.com");
    }

    @Test
    @DisplayName("Hic kullanici yoksa bos liste donmeli")
    void shouldReturnEmptyListWhenNoUsers() {
        // Given
        when(userRepository.findAll()).thenReturn(List.of());

        // When
        List<UserResponse> responses = userService.getAllUsers();

        // Then
        assertThat(responses).isEmpty();
    }

    // --- createUser ---

    @Test
    @DisplayName("Kullanici basariyla olusturulmali ve rol ROLE_MEMBER olmali")
    void shouldCreateUserSuccessfully() {
        // Given
        UserSaveRequest request = new UserSaveRequest("John", "Doe", "john@example.com", "password123");
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        UserResponse response = userService.createUser(request);

        // Then
        assertThat(response.firstName()).isEqualTo("John");
        assertThat(response.role()).isEqualTo(Role.ROLE_MEMBER);
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    // --- updateUser ---

    @Test
    @DisplayName("Kullanici bilgileri basariyla guncellenmeli")
    void shouldUpdateUserSuccessfully() {
        // Given
        UserUpdateRequest request = new UserUpdateRequest("UpdatedName", "UpdatedSurname", "updated@example.com");
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setFirstName("UpdatedName");
        updatedUser.setLastName("UpdatedSurname");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setRole(Role.ROLE_MEMBER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // When
        UserResponse response = userService.updateUser(1L, request);

        // Then
        assertThat(response.firstName()).isEqualTo("UpdatedName");
        assertThat(response.email()).isEqualTo("updated@example.com");
        verify(passwordEncoder, never()).encode(anyString()); // sifre encode edilmemeli
    }

    @Test
    @DisplayName("Olmayan userId ile updateUser RuntimeException firlatmali")
    void shouldThrowExceptionWhenUpdatingNonExistentUser() {
        // Given
        UserUpdateRequest request = new UserUpdateRequest("Name", "Surname", "email@example.com");
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(99L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Kullanıcı bulunamadı!");
    }

    // --- deleteUser ---

    @Test
    @DisplayName("Kullanici basariyla silinmeli")
    void shouldDeleteUserSuccessfully() {
        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository, times(1)).deleteById(1L);
    }
}