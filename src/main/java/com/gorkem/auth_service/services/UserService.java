package com.gorkem.auth_service.services;


import com.gorkem.auth_service.dto.UserResponse;
import com.gorkem.auth_service.dto.UserSaveRequest;

import java.util.List;

public interface UserService {

     UserResponse getOneUser(Long userId);
     List<UserResponse> getAllUsers();
     UserResponse createUser(UserSaveRequest newUserRequest);
     UserResponse updateUser(Long userId, UserSaveRequest updateUserRequest);
    void deleteUser(Long userId);

}
