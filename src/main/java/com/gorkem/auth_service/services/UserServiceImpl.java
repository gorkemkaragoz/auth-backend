package com.gorkem.auth_service.services;

import com.gorkem.auth_service.dto.UserResponse;
import com.gorkem.auth_service.dto.UserSaveRequest;
import com.gorkem.auth_service.entities.Role;
import com.gorkem.auth_service.entities.User;
import com.gorkem.auth_service.repos.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse getOneUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserResponse(user.getId(), user.getFirstName(),
                user.getLastName(), user.getEmail(), user.getRole());
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(user -> new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole())).collect(Collectors.toList());
    }

    @Override
    public UserResponse createUser(UserSaveRequest newUserRequest) {
        User user = new User();
        user.setFirstName(newUserRequest.firstName());
        user.setLastName(newUserRequest.lastName());
        user.setEmail(newUserRequest.email());

        user.setPassword(passwordEncoder.encode(newUserRequest.password()));

        user.setRole(Role.ROLE_MEMBER); // Varsayılan rol

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
    public UserResponse updateUser(Long userId, UserSaveRequest updateUserRequest) {
        User foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        foundUser.setFirstName(updateUserRequest.firstName());
        foundUser.setLastName(updateUserRequest.lastName());
        foundUser.setEmail(updateUserRequest.email());

        foundUser.setPassword(passwordEncoder.encode(updateUserRequest.password()));

        User updatedUser = userRepository.save(foundUser);
        return new UserResponse(
                updatedUser.getId(),
                updatedUser.getFirstName(),
                updatedUser.getLastName(),
                updatedUser.getEmail(),
                updatedUser.getRole()
        );
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

}
