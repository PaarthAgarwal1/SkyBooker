package com.skybooker.PassengerService.service;

import com.skybooker.PassengerService.dto.request.AddPassengerRequest;
import com.skybooker.PassengerService.dto.request.AssignSeatRequest;
import com.skybooker.PassengerService.dto.request.UpdatePassengerRequest;
import com.skybooker.PassengerService.dto.response.PassengerResponse;

import java.util.List;
import java.util.UUID;

public interface PassengerService {

    PassengerResponse addPassenger(AddPassengerRequest request);
    PassengerResponse getPassengerById(UUID passengerId);
    List<PassengerResponse> getPassengerByBooking(UUID bookingId);
    PassengerResponse updatePassenger(UUID passengerId, UpdatePassengerRequest request);
    PassengerResponse assignSeat(UUID id, AssignSeatRequest request);
    long getPassengerCount(UUID bookingId);
    void generateTicketForBooking(UUID bookingId);

    PassengerResponse getByPassportNumber(String passportNumber);

    void deletePassenger(UUID passengerId);

    void deleteByBooking(UUID bookingId);
}
