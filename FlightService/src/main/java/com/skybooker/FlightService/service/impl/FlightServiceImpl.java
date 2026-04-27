package com.skybooker.FlightService.service.impl;

import com.skybooker.FlightService.dto.request.CreateFlightRequest;
import com.skybooker.FlightService.dto.response.FlightResponse;
import com.skybooker.FlightService.entity.Flight;
import com.skybooker.FlightService.entity.FlightStatus;
import com.skybooker.FlightService.exception.BadRequestException;
import com.skybooker.FlightService.exception.ResourceNotFoundException;
import com.skybooker.FlightService.mapper.FlightMapper;
import com.skybooker.FlightService.repository.FlightRepository;
import com.skybooker.FlightService.service.FlightService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class FlightServiceImpl implements FlightService {

    private final FlightRepository flightRepository;


    @Override
    public FlightResponse addFlight(CreateFlightRequest request) {
        flightRepository.findByFlightNumber(request.getFlightNumber())
                .ifPresent(f -> {
                    throw new BadRequestException("Flight already exists");
                });

        Flight flight= FlightMapper.toEntity(request);
        return FlightMapper.toResponse(flightRepository.save(flight));
    }

    @Override
    public FlightResponse getFlightById(UUID id) throws ResourceNotFoundException {
        Flight f=flightRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Flight not found"));
        return FlightMapper.toResponse(f);
    }

    @Override
    public FlightResponse getFlightByNumber(String flightNumber) throws ResourceNotFoundException {
        Flight f=flightRepository.findByFlightNumber(flightNumber).orElseThrow(()->new ResourceNotFoundException("Flight not found"));
        return FlightMapper.toResponse(f);
    }

    @Override
    public List<FlightResponse> searchFlight(String origin, String destination, LocalDate date) {
        LocalDateTime start=date.atStartOfDay();
        LocalDateTime end=date.atTime(23,59,59);
        return flightRepository.findByOriginAndDestAndDate(origin,destination,start,end)
                .stream().map(FlightMapper::toResponse).toList();
    }

    @Override
    public List<FlightResponse> searchRoundTrip(String origin, String destination, LocalDate departureDate, LocalDate returnDate) {
        List<FlightResponse> outbound=searchFlight(origin,destination,departureDate);
        List<FlightResponse> inbound=searchFlight(destination,origin,returnDate);

        return Stream
                .concat(outbound.stream(),inbound.stream())
                .toList();
    }

    @Override
    public FlightResponse updateFlight(UUID id, CreateFlightRequest request) throws ResourceNotFoundException {
        Flight f = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found"));

        f.setDepartureTime(request.getDepartureTime());
        f.setArrivalTime(request.getArrivalTime());
        f.setAircraftType(request.getAircraftType());

        return FlightMapper.toResponse(flightRepository.save(f));
    }

    @Override
    public void deleteFlight(UUID id) throws ResourceNotFoundException {
        if (!flightRepository.existsById(id)) {
            throw new ResourceNotFoundException("Flight not found");
        }
        flightRepository.deleteById(id);
    }

    @Override
    public Void updateStatus(UUID id, String status) throws BadRequestException, ResourceNotFoundException {
        Flight f = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found"));
        try {
            f.setStatus(Enum.valueOf(FlightStatus.class,status));
        } catch (Exception e) {
            throw new BadRequestException("Invalid status");
        }

        flightRepository.save(f);
        return null;
    }

    @Transactional
    @Override
    public void decrementSeats(UUID id, int count) throws ResourceNotFoundException, BadRequestException {
        Flight f = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found"));

        if (f.getAvailableSeats() < count) {
            throw new BadRequestException("Not enough seats available");
        }

        f.setAvailableSeats(f.getAvailableSeats() - count);
        flightRepository.save(f);
    }

    @Transactional
    @Override
    public void incrementSeats(UUID id, int count) throws ResourceNotFoundException {
        Flight f = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found"));

        f.setAvailableSeats(f.getAvailableSeats() + count);
        flightRepository.save(f);
    }
}
