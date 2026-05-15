package com.skybooker.AirlineAirportService.service.impl;

import com.skybooker.AirlineAirportService.dto.airport.AirportRequest;
import com.skybooker.AirlineAirportService.dto.airport.AirportResponse;
import com.skybooker.AirlineAirportService.entity.Airport;
import com.skybooker.AirlineAirportService.exception.DuplicateResourceException;
import com.skybooker.AirlineAirportService.exception.ResourceNotFoundException;
import com.skybooker.AirlineAirportService.repository.AirportRepository;
import com.skybooker.AirlineAirportService.service.AirportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AirportServiceImpl implements AirportService {

    private final AirportRepository repository;

    @Override
    public AirportResponse createAirport(AirportRequest request) {
        if(repository.existsByIataCode(request.getIataCode())){
            throw new DuplicateResourceException("Airport","IATA Code",request.getIataCode());
        }
        Airport airport=Airport.builder()
                .airportName(request.getAirportName())
                .city(request.getCity())
                .country(request.getCountry())
                .iataCode(request.getIataCode())
                .icaoCode(request.getIcaoCode())
                .timeZone(request.getTimezone())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();
        return map(repository.save(airport));
    }

    @Override
    public AirportResponse getAirportByIata(String code) {
        Airport airport =repository.findByIataCode(code.toUpperCase())
                .orElseThrow(()->new ResourceNotFoundException("Airport","IATA",code));

        return map(airport);
    }

    @Override
    public List<AirportResponse> searchAirports(String query) {
        return repository.searchAirports(query)
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public List<AirportResponse> getAirportsByCity(String city) {
        return repository.findByCity(city)
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public void deleteAirport(UUID id) {
        Airport airport = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Airport", "ID", id.toString()));
        repository.delete(airport);
    }

    @Override
    public AirportResponse updateAirport(UUID id, AirportRequest request) {
        Airport airport = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Airport", "ID", id.toString()));

        airport.setAirportName(request.getAirportName());
        airport.setCity(request.getCity());
        airport.setCountry(request.getCountry());
        airport.setIcaoCode(request.getIcaoCode());
        airport.setTimeZone(request.getTimezone());

        return map(repository.save(airport));
    }

    @Override
    public AirportResponse getAirportById(UUID id) {
        Airport airport = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Airport", "ID", id.toString()));

        return map(airport);
    }

    @Override
    public List<AirportResponse> getAllAirports() {
        return repository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    private AirportResponse map(Airport airport){
        return AirportResponse.builder()
                .airportId(airport.getAirportId())
                .airportName(airport.getAirportName())
                .city(airport.getCity())
                .country(airport.getCountry())
                .iataCode(airport.getIataCode())
                .icaoCode(airport.getIcaoCode())
                .timezone(airport.getTimeZone())
                .build();
    }
}
