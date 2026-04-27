package com.skybooker.FlightService.service;

import com.skybooker.FlightService.dto.request.CreateFlightRequest;
import com.skybooker.FlightService.dto.response.FlightResponse;
import com.skybooker.FlightService.exception.BadRequestException;
import com.skybooker.FlightService.exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface FlightService {

    FlightResponse addFlight(CreateFlightRequest request);

    FlightResponse getFlightById(UUID id) throws ResourceNotFoundException;

    FlightResponse getFlightByNumber(String flightNumber) throws ResourceNotFoundException;

    List<FlightResponse> searchFlight(String origin, String destination, LocalDate date);

    List<FlightResponse> searchRoundTrip(
            String origin,
            String destination,
            LocalDate departureDate,
            LocalDate returnDate
    );

    FlightResponse updateFlight(UUID id,CreateFlightRequest request) throws ResourceNotFoundException;

    void deleteFlight(UUID id) throws ResourceNotFoundException;

    Void updateStatus(UUID id,String status) throws BadRequestException, ResourceNotFoundException;

    void decrementSeats(UUID id,int count) throws ResourceNotFoundException, BadRequestException;

    void incrementSeats(UUID id,int count) throws ResourceNotFoundException;
}
