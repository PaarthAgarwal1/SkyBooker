package com.skybooker.AuthService.client;

import com.skybooker.AuthService.dto.response.AirlineResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "AIRLINE-AIRPORT-SERVICE")
public interface AirlineClient {

    @GetMapping("/airline/{id}")
    AirlineResponse getAirlineById(@PathVariable("id") UUID id);
}
