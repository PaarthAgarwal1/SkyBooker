package com.skybooker.BookingService.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class PassengerFeignRequest {

    private UUID bookingId;
    private String firstName;
    private String lastName;
    private String gender;
    private LocalDate dateOfBirth;
    private String passportNumber;
    private String nationality;
    private UUID seatId;
    private String seatNumber;
    private String passengerType;
    private LocalDate passportExpiry;
}
