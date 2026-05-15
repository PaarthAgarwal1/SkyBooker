package com.skybooker.PassengerService.mapper;

import com.skybooker.PassengerService.dto.response.PassengerResponse;
import com.skybooker.PassengerService.entity.Passenger;

public class PassengerMapper {
    public static PassengerResponse toResponse(Passenger p) {
        return PassengerResponse.builder()
                .passengerId(p.getPassengerId())
                .bookingId(p.getBookingId())
                .title(p.getTitle())
                .firstName(p.getFirstName())
                .lastName(p.getLastName())
                .dateOfBirth(p.getDateOfBirth())
                .gender(p.getGender())
                .passportNumber(p.getPassportNumber())
                .nationality(p.getNationality())
                .passportExpiry(p.getPassportExpiry())
                .seatId(p.getSeatId())
                .seatNumber(p.getSeatNumber())
                .ticketNumber(p.getTicketNumber())
                .passengerType(p.getPassengerType().name())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
