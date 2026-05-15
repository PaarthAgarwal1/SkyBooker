package com.skybooker.PassengerService.service.impl;

import com.skybooker.PassengerService.dto.request.AddPassengerRequest;
import com.skybooker.PassengerService.dto.request.AssignSeatRequest;
import com.skybooker.PassengerService.dto.request.UpdatePassengerRequest;
import com.skybooker.PassengerService.dto.response.PassengerResponse;
import com.skybooker.PassengerService.entity.Passenger;
import com.skybooker.PassengerService.entity.PassengerType;
import com.skybooker.PassengerService.exception.ResourceNotFoundException;
import com.skybooker.PassengerService.mapper.PassengerMapper;
import com.skybooker.PassengerService.repository.PassengerRepository;
import com.skybooker.PassengerService.service.PassengerService;
import com.skybooker.PassengerService.util.TicketGenerator;
import jakarta.ws.rs.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PassengerServiceImpl implements PassengerService {

    private final PassengerRepository repository;

    // ================= ADD PASSENGER =================
    @Override
    public PassengerResponse addPassenger(AddPassengerRequest request) {

        validatePassenger(request.getDateOfBirth(), request.getPassportExpiry());

        Passenger passenger = Passenger.builder()
                .bookingId(request.getBookingId())
                .flightId(request.getFlightId())
                .title(request.getTitle())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .passportNumber(request.getPassportNumber())
                .nationality(request.getNationality())
                .passportExpiry(request.getPassportExpiry())
                .passengerType(getPassengerType(request.getDateOfBirth()))
                .build();

        repository.save(passenger);

        AssignSeatRequest seatRequest=AssignSeatRequest.builder().seatId(request.getSeatId()).seatNumber(request.getSeatNumber()).build();

        return assignSeat(passenger.getPassengerId(),seatRequest);
    }

    // ================= GET BY ID =================
    @Override
    public PassengerResponse getPassengerById(UUID passengerId) {
        return PassengerMapper.toResponse(getPassenger(passengerId));
    }

    // ================= GET BY BOOKING =================
    @Override
    public List<PassengerResponse> getPassengerByBooking(UUID bookingId) {
        return repository.findByBookingId(bookingId)
                .stream()
                .sorted(Comparator.comparing(Passenger::getPassengerType)) // Adult -> Child -> Infant
                .map(PassengerMapper::toResponse)
                .toList();
    }

    // ================= UPDATE PASSENGER =================
    @Override
    public PassengerResponse updatePassenger(UUID passengerId, UpdatePassengerRequest request) {

        Passenger p = getPassenger(passengerId);

        if (request.getTitle() != null) {
            p.setTitle(request.getTitle());
        }
        if (request.getFirstName() != null) {
            p.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            p.setLastName(request.getLastName());
        }
        if (request.getGender() != null) {
            p.setGender(request.getGender());
        }
        if (request.getNationality() != null) {
            p.setNationality(request.getNationality());
        }

        if (request.getDateOfBirth() != null) {
            p.setDateOfBirth(request.getDateOfBirth());
            p.setPassengerType(getPassengerType(request.getDateOfBirth()));
        }

        if (request.getPassportNumber() != null) {
            p.setPassportNumber(request.getPassportNumber());
        }

        if (request.getPassportExpiry() != null) {
            p.setPassportExpiry(request.getPassportExpiry());
        }

        // 🔥 validate after update
        validatePassenger(p.getDateOfBirth(), p.getPassportExpiry());

        return PassengerMapper.toResponse(repository.save(p));
    }

    // ================= ASSIGN SEAT =================
    @Override
    public PassengerResponse assignSeat(UUID id, AssignSeatRequest request) {

        Passenger p = getPassenger(id);

        if (request.getSeatNumber() == null) {
            throw new BadRequestException("Seat number is required");
        }

        p.setSeatId(request.getSeatId());
        p.setSeatNumber(request.getSeatNumber());

        // 🔥 Generate ticket automatically after seat assignment
        if (p.getTicketNumber() == null) {
            p.setTicketNumber(generateUniqueTicket());
        }

        return PassengerMapper.toResponse(repository.save(p));
    }

    // ================= COUNT =================
    @Override
    public long getPassengerCount(UUID bookingId) {
        return repository.countByBookingId(bookingId);
    }

    // ================= GENERATE TICKETS =================
    @Override
    public void generateTicketForBooking(UUID bookingId) {

        List<Passenger> passengers = repository.findByBookingId(bookingId);

        for (Passenger p : passengers) {
            if (p.getTicketNumber() == null) {
                p.setTicketNumber(generateUniqueTicket()); // ✅ FIXED
            }
        }

        repository.saveAll(passengers);
    }

    // ================= GET BY PASSPORT =================
    @Override
    public PassengerResponse getByPassportNumber(String passportNumber) {

        Passenger p = repository.findByPassportNumber(passportNumber);

        if (p == null) {
            throw new ResourceNotFoundException("Passenger not found with passport number");
        }

        return PassengerMapper.toResponse(p);
    }

    // ================= DELETE =================
    @Override
    public void deletePassenger(UUID passengerId) {
        Passenger p = getPassenger(passengerId);
        repository.delete(p);
    }

    @Override
    public void deleteByBooking(UUID bookingId) {
        List<Passenger> passengers = repository.findByBookingId(bookingId);

        if (passengers.isEmpty()) {
            throw new ResourceNotFoundException("No passengers found for booking");
        }

        repository.deleteAll(passengers);
    }

    @Override
    public List<PassengerResponse> getPassengerByFlightId(UUID flightId) {
        return repository.findByFlightId(flightId)
                .stream()
                .sorted(Comparator.comparing(Passenger::getPassengerType)) // Adult -> Child -> Infant
                .map(PassengerMapper::toResponse)
                .toList();
    }

    // ================= HELPER METHODS =================

    private Passenger getPassenger(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger not found"));
    }

    private PassengerType getPassengerType(LocalDate dob) {
        int age = Period.between(dob, LocalDate.now()).getYears();

        if (age < 2) return PassengerType.INFANT;
        if (age <= 12) return PassengerType.CHILD;
        return PassengerType.ADULT;
    }

    private void validatePassenger(LocalDate dob, LocalDate passportExpiry) {

        if (dob == null || dob.isAfter(LocalDate.now())) {
            throw new BadRequestException("Invalid date of birth");
        }

        if (passportExpiry != null && passportExpiry.isBefore(LocalDate.now())) {
            throw new BadRequestException("Passport is expired");
        }
    }

    private String generateUniqueTicket() {
        String ticket;
        do {
            ticket = TicketGenerator.generateTicket();
        } while (repository.existsByTicketNumber(ticket));

        return ticket;
    }
}