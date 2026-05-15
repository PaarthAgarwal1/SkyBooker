package com.skybooker.AuthService.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateProfileDTO {

    @NotBlank
    private String fullName;

    @Pattern(
            regexp = "^$|^\\+?[0-9]{10,15}$",
            message = "Invalid phone number"
    )
    private String phone;

    private String passportNumber;

    private String nationality;
}
