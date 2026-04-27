package com.skybooker.AuthService.dto.request;

import lombok.Data;

@Data
public class UpdateProfileDTO {
    private String fullName;
    private String phone;
    private String passportNumber;
    private String nationality;
}
