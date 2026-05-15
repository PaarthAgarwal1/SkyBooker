package com.skybooker.SeatService.service.impl;

import com.skybooker.SeatService.client.FlightClient;
import com.skybooker.SeatService.dto.request.CreateSeatRequest;
import com.skybooker.SeatService.dto.response.SeatResponse;
import com.skybooker.SeatService.entity.Seat;
import com.skybooker.SeatService.entity.SeatStatus;
import com.skybooker.SeatService.exception.ResourceNotFoundException;
import com.skybooker.SeatService.exception.SeatStatusException;
import com.skybooker.SeatService.mapper.SeatMapper;
import com.skybooker.SeatService.repository.SeatRepository;
import com.skybooker.SeatService.service.SeatService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {
    private final SeatRepository seatRepository;
    private final FlightClient flightClient;

    @Override
    public void addSeats(UUID flightId, List<CreateSeatRequest> requests) {
        List<Seat> seats=requests.stream().map(r->
                Seat.builder()
                        .flightId(flightId)
                        .seatNumber(r.getSeatNumber())
                        .seatClass(r.getSeatClass())
                        .rowNumber(r.getRowNumber())
                        .columnNumber(r.getColumnNumber())
                        .isWindow(r.isWindow())
                        .isAisle(r.isAisle())
                        .hasExtraLegroom(r.isHasExtraLegroom())
                        .priceMultiplier(r.getPriceMultiplier())
                        .build()
        ).toList();
        flightClient.addSeats(flightId,seats.size());
        seatRepository.saveAll(seats);

    }

    @Override
    public List<SeatResponse> getSeatMap(UUID flightId) {
        return seatRepository.findByFlightId(flightId)
                .stream().map(SeatMapper::toResponse).toList();
    }

    @Override
    public List<SeatResponse> getAvailableSeats(UUID flightId) {
        return seatRepository.findAvailableSeats(flightId)
                .stream().map(SeatMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public SeatResponse holdSeat(UUID seatId) {

        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found"));

        System.out.println("Seat Status = " + seat.getStatus());

        if (!SeatStatus.AVAILABLE.equals(seat.getStatus())) {
            throw new SeatStatusException("Seat not available");
        }

        seat.setStatus(SeatStatus.HELD);
        seat.setHoldExpiryTime(LocalDateTime.now().plusMinutes(15));

        return SeatMapper.toResponse(seatRepository.save(seat));
    }

    @Override
    @Transactional
    public SeatResponse confirmSeat(UUID seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(()->new ResourceNotFoundException("Seat not found"));
        if(seat.getStatus()!=SeatStatus.HELD){
            throw new SeatStatusException("Seat not Held");
        }
        seat.setStatus(SeatStatus.CONFIRMED);
        seat.setHoldExpiryTime(null);
        return SeatMapper.toResponse(seatRepository.save(seat));
    }

    @Override
    @Transactional
    public SeatResponse releaseSeat(UUID seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(()->new ResourceNotFoundException("Seat not found"));
        seat.setStatus(SeatStatus.AVAILABLE);
        seat.setHoldExpiryTime(null);
        return SeatMapper.toResponse(seatRepository.save(seat));
    }

    @Override
    public SeatResponse updateSeat(UUID seatId, CreateSeatRequest seatRequest) {
        return null;
    }

    @Override
    public long countAvailable(UUID flightId) {
        return seatRepository.countAvailableSeas(flightId);
    }

    @Override
    public void deleteSeatForFlight(UUID seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(()->new ResourceNotFoundException("Seat not found"));
        seatRepository.delete(seat);
    }

    @Override
    public SeatResponse getSeatById(UUID seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(()->new ResourceNotFoundException("Seat not found"));
        return SeatMapper.toResponse(seat);
    }
}
