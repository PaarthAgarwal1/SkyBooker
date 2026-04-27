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
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PassengerServiceImpl implements PassengerService {

    private final PassengerRepository repository;

    @Override
    public PassengerResponse addPassenger(AddPassengerRequest request) {

        validatePassenger(request.getDateOfBirth(),request.getPassportExpiry());

        Passenger passenger= Passenger.builder()
                .bookingId(request.getBookingId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .passportNumber(request.getPassportNumber())
                .nationality(request.getNationality())
                .passportExpiry(request.getPassportExpiry())
                .passengerType(getPassengerType(request.getDateOfBirth()))
                .build();
        return PassengerMapper.toResponse(repository.save(passenger));
    }

    @Override
    public PassengerResponse getPassengerById(UUID passengerId) {
        return PassengerMapper.toResponse(getPassenger(passengerId));
    }

    @Override
    public List<PassengerResponse> getPassengerByBooking(UUID bookingId) {
        return repository.findByBookingId(bookingId)
                .stream()
                .map(PassengerMapper::toResponse)
                .toList();
    }

    @Override
    public PassengerResponse updatePassenger(UUID passengerId, UpdatePassengerRequest request) {

        Passenger p=getPassenger(passengerId);

        if(request.getFirstName()!=null){
            p.setFirstName(request.getFirstName());
        }
        if (request.getLastName()!=null){
            p.setLastName(request.getLastName());
        }
        if(request.getGender()!=null){
            p.setGender(request.getGender());
        }
        if(request.getNationality()!=null){
            p.setNationality(request.getNationality());
        }

        return PassengerMapper.toResponse(repository.save(p));
    }

    @Override
    public PassengerResponse assignSeat(UUID id, AssignSeatRequest request) {

        Passenger p=getPassenger(id);

        p.setSeatId(request.getSeatId());
        p.setSeatNumber(request.getSeatNumber());

        return PassengerMapper.toResponse(repository.save(p));
    }

    @Override
    public long getPassengerCount(UUID bookingId) {
        return repository.countByBookingId(bookingId);
    }

    @Override
    public void generateTicketForBooking(UUID bookingId) {
        List<Passenger> passengers=repository.findByBookingId(bookingId);
        for (Passenger p:passengers){
            if (p.getTicketNumber()==null){
                String ticket;
                do{
                    ticket= TicketGenerator.generateTicket();
                }while (repository.existsByTicketNumber(ticket));
            }
        }
        repository.saveAll(passengers);
    }

    @Override
    public PassengerResponse getByPassportNumber(String passportNumber) {
        return PassengerMapper.toResponse(repository.findByPassportNumber(passportNumber));
    }

    @Override
    public void deletePassenger(UUID passengerId) {
        Passenger p=getPassenger(passengerId);
        repository.delete(p);
    }

    @Override
    public void deleteByBooking(UUID bookingId) {
        List<Passenger> passengers =repository.findByBookingId(bookingId);
        if (passengers.isEmpty()){
            throw new ResourceNotFoundException("No passenger found for booking");
        }
        repository.deleteAll(passengers);
    }

    private Passenger getPassenger(UUID id){
        return repository.findById(id).orElseThrow(()->new ResourceNotFoundException("Passenger not found"));
    }

    private PassengerType getPassengerType(LocalDate dob){
        int age= Period.between(dob,LocalDate.now()).getYears();

        if(age<2){
            return PassengerType.INFANT;
        }
        if(age<=12){
            return PassengerType.CHILD;
        }
        return PassengerType.ADULT;
    }

    private void validatePassenger(LocalDate dob,LocalDate passportExpiry){
        if (dob.isAfter(LocalDate.now())){
            throw new BadRequestException("Invalid DOB");
        }
        if(passportExpiry.isBefore(LocalDate.now())){
            throw new BadRequestException("Passport Expired");
        }
    }

}
