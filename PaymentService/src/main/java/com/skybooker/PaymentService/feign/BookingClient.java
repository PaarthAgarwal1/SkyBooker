package com.skybooker.PaymentService.feign;

import com.skybooker.PaymentService.dto.request.ConfirmBookingRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "BOOKING-SERVICE")
public interface BookingClient {

    @PostMapping("/bookings/confirm/{bookingId}")
    void confirmBooking(
            @PathVariable UUID bookingId,
            @RequestBody ConfirmBookingRequest request
    );

    @PutMapping("/bookings/cancel/{id}")
    ResponseEntity<String> cancel(@PathVariable UUID id);
}
