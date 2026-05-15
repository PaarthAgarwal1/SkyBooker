package com.skybooker.BookingService.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class BookingEmailRequest {

    private UUID userId;

    private String email;

    // Passenger + Seat together
    private List<String> passengerDetails;

    private String pnr;

    private String flightNumber;

    private String departure;

    private String arrival;

    private double totalFare;
}