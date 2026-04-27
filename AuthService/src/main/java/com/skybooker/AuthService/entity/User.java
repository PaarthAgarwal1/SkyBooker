package com.skybooker.AuthService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue
    private UUID userId;

    private String fullName;

    @Column(unique = true,nullable = false)
    private String email;

    private String passwordHash;

    private String phone;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String provider;

    private boolean isActive=true;

    private String passportNumber;

    private String nationality;

    private LocalDateTime createdAt;

}
