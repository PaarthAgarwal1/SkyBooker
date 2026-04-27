package com.skybooker.AuthService.service;

import com.skybooker.AuthService.dto.request.ChangePasswordDTO;
import com.skybooker.AuthService.dto.request.LoginDTO;
import com.skybooker.AuthService.dto.request.RegisterDTO;
import com.skybooker.AuthService.dto.request.UpdateProfileDTO;
import com.skybooker.AuthService.dto.response.AuthResponse;
import com.skybooker.AuthService.dto.response.UserResponse;
import com.skybooker.AuthService.entity.User;

import java.util.List;

public interface AuthService {
    AuthResponse register(RegisterDTO request);

    AuthResponse login(LoginDTO request);

//    void logout(String token);
//
//    boolean validateToken(String token);
//
//    AuthResponse refreshToken(String token);

    UserResponse getProfile(String email);

    UserResponse updateProfile(String email, UpdateProfileDTO request);

    void changePassword(String email, ChangePasswordDTO request);

    void deactivateAccount(String email);

    List<UserResponse> getAllUsers();

}
