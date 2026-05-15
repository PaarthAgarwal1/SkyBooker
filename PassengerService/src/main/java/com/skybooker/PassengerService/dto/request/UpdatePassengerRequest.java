package com.skybooker.PassengerService.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdatePassengerRequest {

    private String title;

    @Size(min = 1, message = "First name cannot be empty")
    private String firstName;

    @Size(min = 1, message = "Last name cannot be empty")
    private String lastName;

    @Pattern(regexp = "MALE|FEMALE|OTHER", message = "Invalid gender")
    private String gender;

    private String nationality;

    @Past(message = "DOB must be in the past")
    private LocalDate dateOfBirth;

    @Size(min = 6, max = 10, message = "Invalid passport number")
    private String passportNumber;

    @Future(message = "Passport must not be expired")
    private LocalDate passportExpiry;
}