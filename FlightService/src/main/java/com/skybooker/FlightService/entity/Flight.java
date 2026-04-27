package com.skybooker.FlightService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "flights")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flight {

    @Id
    @GeneratedValue
    private UUID flightId;

    @Column(unique = true,nullable = false)
    private String flightNumber;

    private UUID airlineId;

    private String originAirportCode;
    private String destinationAirportCode;

    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;

    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    private FlightStatus status;

    private String aircraftType;

    private Integer totalSeats;
    private Integer availableSeats;

    private BigDecimal basePrice;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
