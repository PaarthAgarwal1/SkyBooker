package com.skybooker.BookingService.controller;

import com.skybooker.BookingService.dto.request.ConfirmBookingRequest;
import com.skybooker.BookingService.dto.request.CreateBookingRequest;
import com.skybooker.BookingService.dto.response.BookingDetailResponse;
import com.skybooker.BookingService.dto.response.BookingResponse;
import com.skybooker.BookingService.dto.response.FareResponse;
import com.skybooker.BookingService.entity.BookingStatus;
import com.skybooker.BookingService.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService service;

    @PostMapping("/create")
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody CreateBookingRequest request){
        return ResponseEntity.ok(service.createBooking(request));
    }

    @PostMapping("/confirm/{bookingId}")
    public ResponseEntity<BookingResponse> confirmBooking(
            @PathVariable UUID bookingId,
            @RequestBody ConfirmBookingRequest request) {

        return ResponseEntity.ok(
                service.confirmBooking(bookingId, request)
        );
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<BookingStatus> getStatus(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                service.getBookingStatus(id)
        );
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> getBookings(){
        return ResponseEntity.ok(service.getAllBookings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getById(@PathVariable UUID id){
        return ResponseEntity.ok(service.getBookingById(id));
    }

    @GetMapping("/pnr/{pnr}")
    public ResponseEntity<BookingResponse> getByPnr(@PathVariable String pnr){
        return ResponseEntity.ok(service.getBookingByPnr(pnr));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponse>> getByUser(@PathVariable UUID userId){
        return ResponseEntity.ok(service.getBookingByUser(userId));
    }

    @GetMapping("/flight/{flightId}")
    public ResponseEntity<List<BookingResponse>> getByFlight(@PathVariable UUID flightId) {
        return ResponseEntity.ok(service.getBookingByFlight(flightId));
    }

    @GetMapping("/upcoming/{userId}")
    public ResponseEntity<List<BookingResponse>> getUpcoming(@PathVariable UUID userId) {
        return ResponseEntity.ok(service.getUpcomingBookings(userId));
    }

    @PutMapping("/cancel/{id}")
    public ResponseEntity<String> cancel(@PathVariable UUID id) {
        service.cancelBooking(id);
        return ResponseEntity.ok("Booking cancelled successfully");
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<String> updateStatus(
            @PathVariable UUID id,
            @RequestParam BookingStatus status) {

        service.updateStatus(id, status);
        return ResponseEntity.ok("Status updated successfully");
    }

    @PostMapping("/calculate-fare")
    public ResponseEntity<FareResponse> calculateFare(
            @RequestBody CreateBookingRequest request) {

        return ResponseEntity.ok(
                service.calculateFarePreview(request)
        );
    }

    @PostMapping("/add-addon/{bookingId}")
    public ResponseEntity<String> addAddOn(
            @PathVariable UUID bookingId,
            @RequestParam String meal,
            @RequestParam int luggageKg) {

        service.addAddOn(bookingId, meal, luggageKg);
        return ResponseEntity.ok("Add-on updated successfully");
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<BookingDetailResponse> getBookingDetails(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getBookingDetails(id));
    }
}
