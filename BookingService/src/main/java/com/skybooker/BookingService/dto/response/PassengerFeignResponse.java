package com.skybooker.BookingService.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class PassengerFeignResponse {

    private UUID passengerId;

    private String title;
    private String firstName;
    private String lastName;

    private String gender;

    private String seatNumber;
    private String ticketNumber;

    private String passengerType;

    private String passportNumber;

    private LocalDate dateOfBirth;

    private String nationality;
    private LocalDate passportExpiry;

    private UUID seatId;

}