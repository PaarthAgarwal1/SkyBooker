package com.skybooker.FlightService.mapper;

import com.skybooker.FlightService.dto.request.CreateFlightRequest;
import com.skybooker.FlightService.dto.response.FlightResponse;
import com.skybooker.FlightService.entity.Flight;
import com.skybooker.FlightService.entity.FlightStatus;
import com.skybooker.FlightService.util.DateUtils;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class FlightMapper {
    private static DateUtils dateUtils;

    public static Flight toEntity(CreateFlightRequest req){
        return Flight.builder()
                .flightNumber(req.getFlightNumber())
                .airlineId(req.getAirlineId())
                .originAirportCode(req.getOriginAirportCode())
                .destinationAirportCode(req.getDestinationAirportCode())
                .departureTime(req.getDepartureTime())
                .arrivalTime(req.getArrivalTime())
                .durationMinutes(DateUtils.calculateDurationMinutes(req.getDepartureTime(),req.getArrivalTime()))
                .aircraftType(req.getAircraftType())
                .totalSeats(req.getTotalSeats())
                .availableSeats(req.getTotalSeats())
                .basePrice(req.getBasePrice())
                .status(FlightStatus.ON_TIME)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static FlightResponse toResponse(Flight f){
        return FlightResponse.builder()
                .flightId(f.getFlightId())
                .flightNumber(f.getFlightNumber())
                .airlineId(f.getAirlineId())
                .originAirportCode(f.getOriginAirportCode())
                .destinationAirportCode(f.getDestinationAirportCode())
                .departureTime(f.getDepartureTime())
                .arrivalTime(f.getArrivalTime())
                .durationMinutes(f.getDurationMinutes())
                .status(f.getStatus().name())
                .aircraftType(f.getAircraftType())
                .availableSeats(f.getAvailableSeats())
                .basePrice(f.getBasePrice())
                .build();
    }
}
