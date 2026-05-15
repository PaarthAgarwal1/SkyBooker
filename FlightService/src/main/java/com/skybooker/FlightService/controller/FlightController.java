package com.skybooker.FlightService.controller;

import com.skybooker.FlightService.dto.request.CreateFlightRequest;
import com.skybooker.FlightService.dto.response.FlightResponse;
import com.skybooker.FlightService.entity.FlightStatus;
import com.skybooker.FlightService.service.FlightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/flights")
@RequiredArgsConstructor
@Tag(name = "Flight APIs", description = "Operations related to flight management and search")
public class FlightController {

    private final FlightService flightService;

    @Operation(summary = "Add Flight", description = "Create a new flight (ADMIN / AIRLINE_STAFF)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Flight created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public FlightResponse addFlight(@Valid @RequestBody CreateFlightRequest request){
        return flightService.addFlight(request);
    }

    @Operation(summary = "Get Flight by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Flight found"),
            @ApiResponse(responseCode = "404", description = "Flight not found")
    })
    @GetMapping("/{id}")
    public FlightResponse getById(@PathVariable UUID id) {
        return flightService.getFlightById(id);
    }

    @Operation(summary = "Get Flight by Flight Number")
    @GetMapping("/number/{no}")
    public FlightResponse getByNumber(@PathVariable String no) {
        return flightService.getFlightByNumber(no);
    }

    @Operation(summary = "Search Flights (One Way)")
    @GetMapping("/search")
    public List<FlightResponse> search(
            @Parameter(description = "Origin airport code") @RequestParam String origin,
            @Parameter(description = "Destination airport code") @RequestParam String destination,
            @Parameter(description = "Departure date (YYYY-MM-DD)") @RequestParam LocalDate date
    ){
        return flightService.searchFlight(origin, destination, date);
    }

    @Operation(summary = "Round Trip Search")
    @GetMapping("/round-trip")
    public List<FlightResponse> roundTrip(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam LocalDate departureDate,
            @RequestParam LocalDate returnDate
    ){
        return flightService.searchRoundTrip(origin, destination, departureDate, returnDate);
    }

    @Operation(summary = "Update Flight")
    @PutMapping("/{id}")
    public FlightResponse update(@PathVariable UUID id,
                                 @RequestBody CreateFlightRequest req) {
        return flightService.updateFlight(id, req);
    }

    @Operation(summary = "Delete Flight")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        flightService.deleteFlight(id);
    }

    @Operation(summary = "Update Flight Status")
    @PutMapping("/status/{id}")
    public void updateStatus(@PathVariable UUID id,
                             @RequestParam FlightStatus status) {
        flightService.updateStatus(id, status);
    }

    @Operation(summary = "Decrement Seats (Booking Service)")
    @PutMapping("/decrement-seats")
    public void decrement(@RequestParam UUID id,
                          @RequestParam int count) {
        flightService.decrementSeats(id, count);
    }

    @Operation(summary = "Increment Seats (Cancellation)")
    @PutMapping("/increment-seats")
    public void increment(@RequestParam UUID id,
                          @RequestParam int count) {
        flightService.incrementSeats(id, count);
    }

    @GetMapping("/airline/{airlineId}")
    public List<FlightResponse> getFlightsByAirline(@PathVariable UUID airlineId) {
        return flightService.getFlightsByAirline(airlineId);
    }

    @PutMapping("/seats")
    public void addSeats(@RequestParam UUID id, @RequestParam int count){
        flightService.addSeats(id,count);
    }
}