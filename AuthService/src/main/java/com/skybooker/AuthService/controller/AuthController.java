package com.skybooker.AuthService.controller;

import com.skybooker.AuthService.dto.request.ChangePasswordDTO;
import com.skybooker.AuthService.dto.request.LoginDTO;
import com.skybooker.AuthService.dto.request.RegisterDTO;
import com.skybooker.AuthService.dto.request.UpdateProfileDTO;
import com.skybooker.AuthService.dto.response.AuthResponse;
import com.skybooker.AuthService.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Controller", description = "Handles authentication and user management APIs")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterDTO request){
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(summary = "Login user and generate JWT")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginDTO request){
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Change user password", security = @SecurityRequirement(name = "BearerAuth"))
    @PostMapping("/password")
    public ResponseEntity<String> changePassword(Authentication authentication,
                                                 @RequestBody ChangePasswordDTO request){
        String email = authentication.getName();
        authService.changePassword(email, request);
        return ResponseEntity.ok("Password Updated Successfully");
    }

    @Operation(summary = "Deactivate user account", security = @SecurityRequirement(name = "BearerAuth"))
    @PutMapping("/deactivate")
    public ResponseEntity<?> deactivate(Authentication auth) {
        authService.deactivateAccount(auth.getName());
        return ResponseEntity.ok("Account deactivated");
    }

    @Operation(summary = "Get user profile", security = @SecurityRequirement(name = "BearerAuth"))
    @GetMapping("/profile")
    public ResponseEntity<?> profile(Authentication auth) {
        return ResponseEntity.ok(authService.getProfile(auth.getName()));
    }

    @Operation(summary = "Update user profile", security = @SecurityRequirement(name = "BearerAuth"))
    @PostMapping("/profile")
    public ResponseEntity<?> update(Authentication authentication,
                                    @RequestBody UpdateProfileDTO request){
        String email = authentication.getName();
        return ResponseEntity.ok(authService.updateProfile(email, request));
    }

    @Operation(summary = "Get all users (Admin only)", security = @SecurityRequirement(name = "BearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @Operation(summary = "Test API endpoint")
    @GetMapping("/api-test")
    public String test(){
        return "This is Test api Welcome!";
    }
}