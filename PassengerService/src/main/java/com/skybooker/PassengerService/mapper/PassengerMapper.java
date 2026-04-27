package com.skybooker.PassengerService.mapper;

import com.skybooker.PassengerService.dto.response.PassengerResponse;
import com.skybooker.PassengerService.entity.Passenger;

public class PassengerMapper {
    public static PassengerResponse toResponse(Passenger p) {
        return PassengerResponse.builder()
                .passengerId(p.getPassengerId())
                .bookingId(p.getBookingId())
                .firstName(p.getFirstName())
                .lastName(p.getLastName())
                .seatNumber(p.getSeatNumber())
                .ticketNumber(p.getTicketNumber())
                .passengerType(p.getPassengerType().name())
                .build();
    }
}
