package com.skybooker.PassengerService.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PassengerResponse {

    private UUID passengerId;
    private UUID bookingId;

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

    private String ticketNumber;

    private String passengerType;

    private LocalDateTime createdAt;
}
