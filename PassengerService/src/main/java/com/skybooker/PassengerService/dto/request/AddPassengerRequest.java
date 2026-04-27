package com.skybooker.PassengerService.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class AddPassengerRequest {
    @NotNull
    private UUID bookingId;

    @NotBlank
    private String firstName;

    private String lastName;

    @NotNull
    private LocalDate dateOfBirth;

    private String gender;

    @NotBlank
    private String passportNumber;

    private String nationality;

    @NotNull
    private LocalDate passportExpiry;
}
