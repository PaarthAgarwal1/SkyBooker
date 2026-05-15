package com.skybooker.PassengerService.controller;

import com.skybooker.PassengerService.dto.request.AddPassengerRequest;
import com.skybooker.PassengerService.dto.request.UpdatePassengerRequest;
import com.skybooker.PassengerService.dto.response.PassengerResponse;
import com.skybooker.PassengerService.service.PassengerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/passengers")
@RequiredArgsConstructor
public class PassengerController {
    private final PassengerService passengerService;

    @PostMapping("/add")
    public ResponseEntity<PassengerResponse> addPassenger(@Valid @RequestBody AddPassengerRequest request){
        return ResponseEntity.ok(passengerService.addPassenger(request));
    }

    @GetMapping("/{passengerId}")
    public ResponseEntity<PassengerResponse> getPassengerById(@PathVariable UUID passengerId){
        return ResponseEntity.ok(passengerService.getPassengerById(passengerId));
    }

    @GetMapping("/flight/{flightId}")
    public ResponseEntity<List<PassengerResponse>> getPassengerBuFlightId(@PathVariable UUID flightId){
        return ResponseEntity.ok(passengerService.getPassengerByFlightId(flightId));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<PassengerResponse>> getByBooking(@PathVariable UUID bookingId){
        return ResponseEntity.ok(passengerService.getPassengerByBooking(bookingId));
    }

    @GetMapping("/passport/{passportNumber}")
    public ResponseEntity<PassengerResponse> getByPassport(@PathVariable String passportNumber){
        return ResponseEntity.ok(passengerService.getByPassportNumber(passportNumber));
    }

    @GetMapping("/count/{bookingId}")
    public ResponseEntity<Long> getCountByBooking(@PathVariable UUID bookingId){
        return ResponseEntity.ok(passengerService.getPassengerCount(bookingId));
    }

    @PutMapping("/update/{passengerId}")
    public ResponseEntity<PassengerResponse> update(@PathVariable UUID passengerId,
                                                    @Valid @RequestBody UpdatePassengerRequest request){
        return ResponseEntity.ok(passengerService.updatePassenger(passengerId,request));
    }

    @DeleteMapping("/{passengerId}")
    public ResponseEntity<String> delete(@PathVariable UUID passengerId){
        passengerService.deletePassenger(passengerId);
        return ResponseEntity.ok("Passenger delete successfully");
    }

    @DeleteMapping("/booking/{bookingId}")
    public ResponseEntity<String> deleteByBooking(@PathVariable UUID bookingId){
        passengerService.deleteByBooking(bookingId);
        return ResponseEntity.ok("All passenger deleted for booking");
    }
}
