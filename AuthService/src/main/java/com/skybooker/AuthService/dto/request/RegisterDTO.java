package com.skybooker.AuthService.dto.request;

import com.skybooker.AuthService.entity.Role;
import lombok.Data;

@Data
public class RegisterDTO {
    private String fullName;
    private String email;
    private String password;
    private String registrationKey;
}
