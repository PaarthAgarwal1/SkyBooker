package com.skybooker.AuthService.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class AuthResponse {
    private String jwtToken;
    private String message;
}
