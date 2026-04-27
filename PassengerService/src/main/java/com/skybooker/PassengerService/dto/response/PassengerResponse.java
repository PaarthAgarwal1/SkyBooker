package com.skybooker.PassengerService.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PassengerResponse {
    private UUID passengerId;
    private UUID bookingId;

    private String firstName;
    private String lastName;

    private String seatNumber;
    private String ticketNumber;

    private String passengerType;
}
