package com.skybooker.AuthService.dto.response;

import com.skybooker.AuthService.entity.Role;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserResponse {

    private UUID userId;

    private String fullName;

    private String email;

    private String phone;

    private Role role;

    private String provider;

    private boolean isActive=true;

    private String passportNumber;

    private String nationality;

    private LocalDateTime createdAt;

}
