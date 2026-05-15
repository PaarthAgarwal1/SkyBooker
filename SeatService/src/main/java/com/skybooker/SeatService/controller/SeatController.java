package com.skybooker.SeatService.controller;

import com.skybooker.SeatService.dto.request.CreateSeatRequest;
import com.skybooker.SeatService.dto.response.SeatResponse;
import com.skybooker.SeatService.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/seats")
@RequiredArgsConstructor
public class SeatController {
    private final SeatService service;

    @PostMapping("/add/{flightId}")
    public ResponseEntity<String> addSeats(@PathVariable UUID flightId,
                                   @RequestBody List<CreateSeatRequest> seats){
        service.addSeats(flightId,seats);
        return ResponseEntity.ok("Seats Added Successfully!!");
    }

    @GetMapping("{seatId}")
    public SeatResponse getSeatById(@PathVariable UUID seatId){
        return service.getSeatById(seatId);
    }

    @GetMapping("/flight/{flightId}")
    public List<SeatResponse> getSeatMap(@PathVariable UUID flightId){
        return service.getSeatMap(flightId);
    }

    @GetMapping("/available/{flightId}")
    public List<SeatResponse> getAvailable(@PathVariable UUID flightId){
        return service.getAvailableSeats(flightId);
    }

    @PutMapping("/hold/{seatId}")
    public SeatResponse hold(@PathVariable UUID seatId) {
        return service.holdSeat(seatId);
    }

    @PutMapping("/confirm/{seatId}")
    public SeatResponse confirm(@PathVariable UUID seatId) {
        return service.confirmSeat(seatId);
    }

    @PutMapping("/release/{seatId}")
    public SeatResponse release(@PathVariable UUID seatId) {
        return service.releaseSeat(seatId);
    }

    @GetMapping("/count/{flightId}")
    public long count(@PathVariable UUID flightId) {
        return service.countAvailable(flightId);
    }

}
