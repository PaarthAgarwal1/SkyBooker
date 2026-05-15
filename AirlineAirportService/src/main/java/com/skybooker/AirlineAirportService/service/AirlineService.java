package com.skybooker.AirlineAirportService.service;

import com.skybooker.AirlineAirportService.dto.airline.AirlineRequest;
import com.skybooker.AirlineAirportService.dto.airline.AirlineResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AirlineService {

    AirlineResponse createAirline(AirlineRequest request);

    AirlineResponse getAirlineById(UUID airlineId);

    AirlineResponse getAirlineByIata(String code);

    List<AirlineResponse> getAllAirlines();

    AirlineResponse updateAirline(UUID id,AirlineRequest request);

    void deactivateAirline(UUID id);

    void activateAirline(UUID id);

    void deleteAirline(UUID id);

}
