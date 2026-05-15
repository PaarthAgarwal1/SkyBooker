package com.skybooker.AirlineAirportService.service;

import com.skybooker.AirlineAirportService.dto.airport.AirportRequest;
import com.skybooker.AirlineAirportService.dto.airport.AirportResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AirportService {

    AirportResponse createAirport(AirportRequest request);

    AirportResponse getAirportByIata(String code);

    List<AirportResponse> searchAirports(String query);

    AirportResponse updateAirport(UUID id,AirportRequest request);

    AirportResponse getAirportById(UUID id);

    List<AirportResponse> getAllAirports();

    List<AirportResponse> getAirportsByCity(String city);

    void deleteAirport(UUID id);
}
