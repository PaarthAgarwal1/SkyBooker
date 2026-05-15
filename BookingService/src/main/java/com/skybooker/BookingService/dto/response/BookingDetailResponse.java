package com.skybooker.BookingService.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class BookingDetailResponse {

    private UUID bookingId;
    private String pnr;
    private String status;

    private String route;
    private String airline;
    private UUID flightId;
    private String departureTime;
    private String arrivalTime;

    private List<PassengerDetail> passengers;

    private double totalFare;
    private double baseFare;
    private double taxes;

    private String mealPreference;
    private int luggageKg;

    private String contactEmail;
    private String contactPhone;

    private LocalDateTime bookedAt;

    @Data
    @Builder
    public static class PassengerDetail {
        private String name;
        private String gender;
        private String seatNumber;
        private String passportNumber;
    }
}