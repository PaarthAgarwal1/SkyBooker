package com.skybooker.NotificationService.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BookingEmailRequest {

    private UUID userId;

    private String email;

    // Combined Passenger + Seat
    private List<String> passengerDetails;

    private String pnr;

    private String flightNumber;

    private String departure;

    private String arrival;

    private double totalFare;
}