package com.skybooker.SeatService.service;

import com.skybooker.SeatService.dto.request.CreateSeatRequest;
import com.skybooker.SeatService.dto.response.SeatResponse;

import java.util.List;
import java.util.UUID;

public interface SeatService {
    void addSeats(UUID flightId, List<CreateSeatRequest> seats);

    List<SeatResponse> getSeatMap(UUID flightId);

    List<SeatResponse> getAvailableSeats(UUID flightId);

    SeatResponse holdSeat(UUID seatId);

    SeatResponse confirmSeat(UUID seatId);

    SeatResponse releaseSeat(UUID seatId);

    SeatResponse updateSeat(UUID seatId,CreateSeatRequest seatRequest);

    long countAvailable(UUID flightId);

    void deleteSeatForFlight(UUID seatId);

    SeatResponse getSeatById(UUID seatId);
}
