package com.skybooker.FlightService.client;


import com.skybooker.FlightService.dto.response.AirlineResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "AIRLINE-AIRPORT-SERVICE")
public interface AirlineClient {

    @GetMapping("/airline/{airlineId}")
    AirlineResponse getAirline(@PathVariable UUID airlineId);

}