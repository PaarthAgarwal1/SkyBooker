package com.skybooker.BookingService.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class SeatFeignResponse {
    private UUID seatId;
    private String seatNumber;
    private String status;
    private double priceMultiplier;
}
