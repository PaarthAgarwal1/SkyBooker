package com.skybooker.BookingService.dto.request;

import com.skybooker.BookingService.entity.TripType;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateBookingRequest {

    private String idempotencyKey;

    private UUID userId;
    private UUID flightId;

    private TripType tripType;

    private String contactEmail;
    private String contactPhone;

    // ✅ CRITICAL: seats selected by user
    private List<UUID> seatIds;

    // ✅ CRITICAL: passenger details
    private List<AddPassengerRequest> passengers;

    // 🎒 Add-ons
    private String mealPreference;
    private int luggageKg;
}