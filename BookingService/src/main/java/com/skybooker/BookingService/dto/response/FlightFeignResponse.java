package com.skybooker.BookingService.dto.response;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class FlightFeignResponse {
    private UUID flightId;
    private String flightNumber;
    private UUID airlineId;
    private String airlineName;
    private String originAirportCode;
    private String destinationAirportCode;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private Integer durationMinutes;
    private String status;
    private String aircraftType;
    private Integer availableSeats;
    private BigDecimal basePrice;
}
