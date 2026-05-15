package com.skybooker.AuthService.service;

import com.skybooker.AuthService.dto.request.ChangePasswordDTO;
import com.skybooker.AuthService.dto.request.LoginDTO;
import com.skybooker.AuthService.dto.request.RegisterDTO;
import com.skybooker.AuthService.dto.request.UpdateProfileDTO;
import com.skybooker.AuthService.dto.response.AuthResponse;
import com.skybooker.AuthService.dto.response.UserResponse;
import com.skybooker.AuthService.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

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

    UserResponse updateProfileImage(String email, MultipartFile file);

    void changePassword(String email, ChangePasswordDTO request);

    void deactivateAccount(String email);

    List<UserResponse> getAllUsers();

    UserResponse getUserById(UUID userId);

    void adminActivateUser(UUID userId);

    void adminDeactivateUser(UUID userId);

    void deleteUser(UUID userId);


    void assignAirline(UUID userId, UUID airlineId);
}
