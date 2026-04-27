package com.skybooker.NotificationService.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class BookingEmailRequest {
    private UUID userId;
    private String email;

    private String passengerName;
    private String pnr;
    private String flightNumber;

    private String departure;
    private String arrival;
    private String seatNumber;

    private double totalFare;
}
