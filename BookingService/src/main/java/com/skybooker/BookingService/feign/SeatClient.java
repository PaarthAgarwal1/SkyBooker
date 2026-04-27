package com.skybooker.BookingService.feign;

import com.skybooker.BookingService.dto.response.SeatFeignResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "SEAT-SERVICE")
public interface SeatClient {
    @PutMapping("/seats/hold/{seatId}")
    SeatFeignResponse holdSeat(@PathVariable UUID seatId);

    @PutMapping("/seats/confirm/{seatId}")
    SeatFeignResponse confirmSeat(@PathVariable UUID seatId);

    @PutMapping("/seats/release/{seatId}")
    SeatFeignResponse releaseSeat(@PathVariable UUID seatId);

    @GetMapping("/seats/count/{flightId}")
    long countAvailable(@PathVariable UUID flightId);
}
