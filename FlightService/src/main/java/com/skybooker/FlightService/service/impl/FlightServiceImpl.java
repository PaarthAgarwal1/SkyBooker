package com.skybooker.FlightService.service.impl;

import com.skybooker.FlightService.client.AirlineClient;
import com.skybooker.FlightService.dto.request.CreateFlightRequest;
import com.skybooker.FlightService.dto.response.AirlineResponse;
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
    private final AirlineClient airlineClient;


    @Override
    public FlightResponse addFlight(CreateFlightRequest request) {

        // ✅ Prevent duplicate flight
        flightRepository.findByFlightNumber(request.getFlightNumber())
                .ifPresent(f -> {
                    throw new BadRequestException("Flight already exists");
                });

        // ✅ Business validation
        if (request.getOriginAirportCode().equals(request.getDestinationAirportCode())) {
            throw new BadRequestException("Origin and destination cannot be same");
        }

        if (request.getArrivalTime().isBefore(request.getDepartureTime())) {
            throw new BadRequestException("Arrival must be after departure");
        }

        if (request.getDepartureTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Departure must be in future");
        }

        // ❌ REMOVE THIS (optional but recommended later)
        AirlineResponse airline = airlineClient.getAirline(request.getAirlineId());

        Flight flight = FlightMapper.toEntity(request);

        // ✅ System fields
        flight.setAirlineName(airline.getAirlineName());
        flight.setStatus(FlightStatus.ON_TIME);
        flight.setAvailableSeats(request.getTotalSeats());
        flight.setCreatedAt(LocalDateTime.now());


        // ✅ Calculate duration
        long duration = java.time.Duration.between(
                request.getDepartureTime(),
                request.getArrivalTime()
        ).toMinutes();

        flight.setDurationMinutes((int) duration);

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
    public FlightResponse updateFlight(UUID id, CreateFlightRequest request) {

        System.out.println("request comes here from frontend "+request);

        Flight f = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found"));

        if (request.getOriginAirportCode() != null)
            f.setOriginAirportCode(request.getOriginAirportCode());

        if (request.getDestinationAirportCode() != null)
            f.setDestinationAirportCode(request.getDestinationAirportCode());

        if (request.getDepartureTime() != null)
            f.setDepartureTime(request.getDepartureTime());

        if (request.getArrivalTime() != null)
            f.setArrivalTime(request.getArrivalTime());

        if (request.getAircraftType() != null)
            f.setAircraftType(request.getAircraftType());

        if (request.getTotalSeats() != null) {
            int diff = request.getTotalSeats() - f.getTotalSeats();
            f.setTotalSeats(request.getTotalSeats());
            f.setAvailableSeats(f.getAvailableSeats() + diff);
        }
        if(request.getStatus()!=null){
            f.setStatus(request.getStatus());
        }

        if (request.getBasePrice() != null)
            f.setBasePrice(request.getBasePrice());

        // ✅ Recalculate duration if times changed
        if (request.getDepartureTime() != null && request.getArrivalTime() != null) {
            long duration = java.time.Duration.between(
                    f.getDepartureTime(),
                    f.getArrivalTime()
            ).toMinutes();
            f.setDurationMinutes((int) duration);

        }

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
    public Void updateStatus(UUID id, FlightStatus status) throws BadRequestException, ResourceNotFoundException {
        Flight f = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found"));
        try {
            f.setStatus(status);
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

    @Override
    public List<FlightResponse> getFlightsByAirline(UUID airlineId) {
        return flightRepository.findByAirlineId(airlineId).stream().map(FlightMapper::toResponse).toList();
    }

    @Override
    public void addSeats(UUID id, int count) {
        Flight f = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found"));
        f.setTotalSeats(count);
        f.setAvailableSeats(count);
        flightRepository.save(f);
    }
}
