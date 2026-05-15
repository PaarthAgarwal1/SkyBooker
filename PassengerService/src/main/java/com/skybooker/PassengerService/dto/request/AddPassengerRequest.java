package com.skybooker.PassengerService.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class AddPassengerRequest {

    @NotNull(message = "Booking ID is required")
    private UUID bookingId;

    private UUID flightId;

    @NotBlank(message = "Title is required")
    private String title; // Mr, Ms, Mrs

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "DOB must be in the past")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Gender is required")
    @Pattern(regexp = "MALE|FEMALE|OTHER", message = "Invalid gender")
    private String gender;

    @NotBlank(message = "Passport number is required")
    @Size(min = 6, max = 10, message = "Invalid passport number length")
    private String passportNumber;

    @NotBlank(message = "Nationality is required")
    private String nationality;

    private UUID seatId;
    private String seatNumber;

    @NotNull(message = "Passport expiry is required")
    @Future(message = "Passport must not be expired")
    private LocalDate passportExpiry;
}