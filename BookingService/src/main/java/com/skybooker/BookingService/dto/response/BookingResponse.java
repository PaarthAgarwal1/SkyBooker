package com.skybooker.BookingService.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class BookingResponse {

    private UUID id;              // rename bookingId → id
    private String pnr;           // rename pnrCode → pnr
    private List<String> passenger;     // add this
    private String route;         // add this
    private double amount;        // rename totalFare → amount
    private String status;
}
