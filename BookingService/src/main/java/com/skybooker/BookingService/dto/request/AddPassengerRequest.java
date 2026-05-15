package com.skybooker.BookingService.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class AddPassengerRequest {
    private String title;
    private String firstName;
    private String lastName;

    private LocalDate dateOfBirth;
    private String gender;

    private String passportNumber;
    private String nationality;
    private LocalDate passportExpiry;

    private UUID seatId;
    private String seatNumber;

    private String passengerType;

}