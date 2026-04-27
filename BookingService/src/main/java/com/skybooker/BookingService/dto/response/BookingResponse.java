package com.skybooker.BookingService.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class BookingResponse {
    private UUID bookingId;
    private String pnrCode;
    private String status;
    private double totalFare;
}
