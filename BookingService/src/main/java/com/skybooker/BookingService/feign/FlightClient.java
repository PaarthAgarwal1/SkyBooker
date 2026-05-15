package com.skybooker.BookingService.feign;

import com.skybooker.BookingService.dto.response.FlightFeignResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "FLIGHT-SERVICE")
public interface FlightClient {

    @GetMapping("/flights/{id}")
    FlightFeignResponse getFlight(@PathVariable UUID id);

    @PutMapping("/flights/decrement-seats")
    void decrementSeats(@RequestParam UUID id, @RequestParam int count);

    @PutMapping("/flights/increment-seats")
    void incrementSeats(@RequestParam UUID id, @RequestParam int count);
}
