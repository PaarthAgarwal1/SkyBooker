package com.skybooker.FlightService.dto.request;

import com.skybooker.FlightService.entity.FlightStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreateFlightRequest {

    @NotBlank
    private String flightNumber;

    @NotNull
    private UUID airlineId;

    @NotBlank
    private String originAirportCode;

    @NotBlank
    private String destinationAirportCode;

    @NotNull
    private LocalDateTime departureTime;

    @NotNull
    private LocalDateTime arrivalTime;

    @NotBlank
    private String aircraftType;

    @Min(1)
    private Integer totalSeats;

    @DecimalMin("0.0")
    private BigDecimal basePrice;

    @NotNull
    private FlightStatus status;
}
