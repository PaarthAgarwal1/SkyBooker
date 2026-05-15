package com.skybooker.BookingService.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class ConfirmBookingRequest {
    private UUID paymentId;
}