package com.skybooker.BookingService.dto.request;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@Data
public class PassengerFeignRequest {
    private UUID bookingId;
    private UUID flightId;
    private String title;
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
