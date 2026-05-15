package com.skybooker.AirlineAirportService.service.impl;

import com.skybooker.AirlineAirportService.dto.airline.AirlineRequest;
import com.skybooker.AirlineAirportService.dto.airline.AirlineResponse;
import com.skybooker.AirlineAirportService.entity.Airline;
import com.skybooker.AirlineAirportService.exception.DuplicateResourceException;
import com.skybooker.AirlineAirportService.exception.ResourceNotFoundException;
import com.skybooker.AirlineAirportService.repository.AirlineRepository;
import com.skybooker.AirlineAirportService.service.AirlineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AirlineServiceImpl implements AirlineService {

    private final AirlineRepository repository;

    @Override
    public AirlineResponse createAirline(AirlineRequest request) {
        if (repository.existsByIataCode(request.getIataCode())) {
            throw new DuplicateResourceException("Airline", "IATA Code", request.getIataCode());
        }

        Airline airline = Airline.builder()
                .airlineName(request.getAirlineName())
                .iataCode(request.getIataCode().toUpperCase())
                .country(request.getCountry())
                .logoUrl(request.getLogoUrl())
                .isActive(true)
                .build();

        return map(repository.save(airline));
    }

    @Override
    public AirlineResponse getAirlineById(UUID airlineId) {
        Airline airline = repository.findById(airlineId)
                .orElseThrow(() -> new ResourceNotFoundException("Airline", "ID", airlineId.toString()));

        return map(airline);
    }

    @Override
    public AirlineResponse getAirlineByIata(String code) {
        Airline airline = repository.findByIataCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Airline", "IATA", code));

        return map(airline);
    }

    @Override
    public List<AirlineResponse> getAllAirlines() {
        return repository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public AirlineResponse updateAirline(UUID id, AirlineRequest request) {
        Airline airline = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Airline", "ID", id.toString()));

        airline.setAirlineName(request.getAirlineName());
        airline.setCountry(request.getCountry());
        airline.setLogoUrl(request.getLogoUrl());

        return map(repository.save(airline));
    }

    @Override
    public void deactivateAirline(UUID id) {
        Airline airline = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Airline", "ID", id.toString()));

        airline.setActive(false);
        repository.save(airline);
    }

    @Override
    public void activateAirline(UUID id) {
        Airline airline = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Airline", "ID", id.toString()));

        airline.setActive(true);
        repository.save(airline);
    }

    @Override
    public void deleteAirline(UUID id) {
        Airline airline = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Airline", "ID", id.toString()));
        repository.delete(airline);
    }

    private AirlineResponse map(Airline airline){
        return AirlineResponse.builder()
                .airlineId(airline.getAirlineId())
                .airlineName(airline.getAirlineName())
                .iataCode(airline.getIataCode())
                .country(airline.getCountry())
                .logoUrl(airline.getLogoUrl())
                .isActive(airline.isActive())
                .build();
    }
}
