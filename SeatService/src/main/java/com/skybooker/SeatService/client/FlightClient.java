package com.skybooker.SeatService.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "FLIGHT-SERVICE")
public interface FlightClient {

    @PutMapping("/flights/seats")
    public void addSeats(@RequestParam UUID id,@RequestParam int count);
}