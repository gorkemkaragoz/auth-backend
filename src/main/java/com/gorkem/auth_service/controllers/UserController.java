package com.gorkem.auth_service.controllers;

import com.gorkem.auth_service.dto.UserResponse;
import com.gorkem.auth_service.dto.UserSaveRequest;
import com.gorkem.auth_service.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("{userId}")
    public UserResponse getOneUser(@PathVariable Long userId){
        return userService.getOneUser(userId);
    }

    @GetMapping
    public List<UserResponse> getAllUsers(){
        return userService.getAllUsers();
    }

    @PostMapping
    public UserResponse createUser(@RequestBody UserSaveRequest newUserRequest){
        return userService.createUser(newUserRequest);
    }

    @PutMapping("{userId}")
    public UserResponse updateUser(@PathVariable Long userId, @RequestBody UserSaveRequest updateUserRequest){
        return userService.updateUser(userId,updateUserRequest);
    }

    @DeleteMapping("{userId}")
    public void deleteUser(@PathVariable Long userId){
        userService.deleteUser(userId);
    }

}
