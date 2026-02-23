package com.gorkem.auth_service.services;


import com.gorkem.auth_service.dto.UserResponse;
import com.gorkem.auth_service.dto.UserSaveRequest;
import com.gorkem.auth_service.dto.UserUpdateRequest;

import java.util.List;

public interface UserService {

     UserResponse getOneUser(Long userId);
     List<UserResponse> getAllUsers();
     UserResponse createUser(UserSaveRequest newUserRequest);
     UserResponse updateUser(Long userId, UserUpdateRequest updateUserRequest);
    void deleteUser(Long userId);

}
